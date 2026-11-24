package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;

/**
 * A triggered ability a step or phase beginning may trigger ({@link TriggerIndex#stepCandidates}): its source as
 * {@link TriggerCandidate} names it, and, for an ability of a face-up plane or phenomenon, of an emblem or of a delayed
 * triggered ability, that object.
 *
 * @param candidate      the ability and its source; a face-up scheme's source is its permanent view (CR 314.4)
 * @param plane          the face-up planar object whose ability it is, or {@code null}
 * @param emblem         the emblem whose ability it is (CR 114.4), or {@code null}
 * @param counterAbility the upkeep ability a card in exile has for the counters on it, or {@code null}
 * @param delayed        the delayed triggered ability a resolving spell or ability made (CR 603.7a), or {@code null}
 */
public record StepCandidate(TriggerCandidate candidate, PlanarObject plane, Emblem emblem,
                            ExileCounterAbility counterAbility, TemporaryGlobalTriggeredAbility delayed) {

    public StepCandidate(TriggerCandidate candidate, PlanarObject plane, Emblem emblem) {
        this(candidate, plane, emblem, null, null);
    }

    public StepCandidate(TriggerCandidate candidate, PlanarObject plane, Emblem emblem,
                         ExileCounterAbility counterAbility) {
        this(candidate, plane, emblem, counterAbility, null);
    }

    /** {@code delayed}'s step ability, with its maker's source card and no source permanent (CR 603.7d, 603.7e). */
    static StepCandidate of(TemporaryGlobalTriggeredAbility delayed) {
        return new StepCandidate(TriggerCandidate.of(delayed), null, null, null, delayed);
    }

    /**
     * The "at the beginning of your upkeep" ability a card in its owner's exile has for the counters on it: what the
     * stack entry is called and what the log says as it triggers.
     */
    public enum ExileCounterAbility {
        /** "If this card is exiled, remove a refine counter from it", gained as it was exiled (Uvilda). */
        REFINE("refine counter ability", "'s refine counter ability triggers."),
        /** Suspend's "if this card is suspended, remove a time counter from it" (CR 702.62a). */
        SUSPEND("suspend ability", "'s suspend ability triggers.");

        private final String kind;
        private final String logText;

        ExileCounterAbility(String kind, String logText) {
            this.kind = kind;
            this.logText = logText;
        }

        /** What the stack entry calls the ability: "Uvilda's refine counter ability". */
        public String kind() {
            return kind;
        }

        /** What the log says after the card's name as the ability triggers. */
        public String logText() {
            return logText;
        }
    }
}
