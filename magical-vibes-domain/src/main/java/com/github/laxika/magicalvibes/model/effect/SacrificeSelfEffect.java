package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifices the stack entry's source permanent ("sacrifice this creature") or, for
 * {@link #ofTriggeringPermanent()}, the permanent whose event produced this triggered ability ("sacrifice that
 * permanent", read from the stack entry's triggering permanent; Grafted Wargear, Stitcher's Graft, the delayed
 * "sacrifice it at the beginning of the next end step" triggers). The default form requires the ability's
 * controller to still control that permanent; the flagged form instructs its current controller to sacrifice it
 * instead ("that creature's controller sacrifices it", Basalt Golem). An optional token rider is created for that
 * player only when the sacrifice succeeds ("if the player does, they create ..."). If the permanent has left the
 * battlefield, nothing happens.
 *
 * @param sacrificed {@link PermanentReference#SOURCE} or {@link PermanentReference#TRIGGERING}
 */
public record SacrificeSelfEffect(PermanentReference sacrificed,
                                  boolean currentControllerSacrifices,
                                  CreateTokenEffect tokenForSacrificingPlayer)
        implements CardEffect {

    public SacrificeSelfEffect {
        if (sacrificed != PermanentReference.SOURCE && sacrificed != PermanentReference.TRIGGERING) {
            throw new IllegalArgumentException("Sacrifices the source or the triggering permanent: " + sacrificed);
        }
    }

    public SacrificeSelfEffect() {
        this(false, null);
    }

    public SacrificeSelfEffect(boolean currentControllerSacrifices) {
        this(currentControllerSacrifices, null);
    }

    public SacrificeSelfEffect(boolean currentControllerSacrifices, CreateTokenEffect tokenForSacrificingPlayer) {
        this(PermanentReference.SOURCE, currentControllerSacrifices, tokenForSacrificingPlayer);
    }

    /** "Sacrifice that permanent": the stack entry's triggering permanent, by the ability's controller. */
    public static SacrificeSelfEffect ofTriggeringPermanent() {
        return ofTriggeringPermanent(false, null);
    }

    /**
     * The triggering permanent, sacrificed by its current controller when {@code currentControllerSacrifices},
     * with {@code tokenForSacrificingPlayer} (or {@code null}) created for that player if they do.
     */
    public static SacrificeSelfEffect ofTriggeringPermanent(boolean currentControllerSacrifices,
                                                            CreateTokenEffect tokenForSacrificingPlayer) {
        return new SacrificeSelfEffect(PermanentReference.TRIGGERING, currentControllerSacrifices,
                tokenForSacrificingPlayer);
    }

    /** Whether this sacrifices the ability's own source permanent rather than the triggering permanent. */
    public boolean sacrificesSource() {
        return sacrificed == PermanentReference.SOURCE;
    }

    @Override
    public TargetSpec targetSpec() {
        // The source form implicitly acts on its own source permanent — so trigger collectors that key off
        // selfTargeting() (e.g. the spell-cast collector) carry the source id onto the stack entry.
        return sacrificesSource() ? new TargetSpec(null, false, null, true, 1) : TargetSpec.NONE;
    }
}
