package com.github.laxika.magicalvibes.model.condition;

/**
 * True when a permanent is phased out by the source "for as long as it remains tapped"
 * ({@code PhaseOutTargetPermanentWhileSourceTappedEffect}, The Pandorica): the event condition of the source's "when
 * this becomes untapped, that permanent phases in", which exists only once the source has phased a permanent out.
 */
public record SourceKeepsPermanentPhasedOut() implements Condition {

    @Override
    public String conditionName() {
        return "a permanent is phased out by this for as long as it remains tapped";
    }

    @Override
    public String conditionNotMetReason() {
        return "no permanent is phased out by this";
    }
}
