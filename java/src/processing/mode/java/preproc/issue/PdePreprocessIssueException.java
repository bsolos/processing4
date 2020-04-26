package processing.mode.java.preproc.issue;


public class PdePreprocessIssueException extends RuntimeException {

  private final PdePreprocessIssue preprocessIssue;

  public PdePreprocessIssueException(PdePreprocessIssue newPreprocessIssue) {
    super(newPreprocessIssue.getMsg());
    preprocessIssue = newPreprocessIssue;
  }

  public PdePreprocessIssue getIssue() {
    return preprocessIssue;
  }

}
