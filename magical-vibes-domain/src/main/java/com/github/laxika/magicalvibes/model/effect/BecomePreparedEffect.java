package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Secrets of Strixhaven "Prepared": the source permanent becomes prepared.
 * <p>
 * When this resolves, a copy of the source's prepare spell (its {@code backFaceCard}) is created in
 * exile with a non-expiring play permission for the source's controller, and the source permanent is
 * marked prepared. While prepared, the controller may cast that exiled copy; doing so unprepares the
 * permanent (handled at cast time, not resolution). A permanent that is already prepared can't become
 * prepared again, so this is a no-op in that case (CR-style: never more than one prepare copy at once).
 * <p>
 * This is normally a self-targeting effect so the trigger framework stamps the source permanent id
 * onto the resolving stack entry (see {@code SpellCastTriggerCollectorService}). When granted to
 * another permanent, the granting permanent id is retained on the effect instead.
 */
public record BecomePreparedEffect(UUID grantingPermanentId) implements GrantingPermanentAwareEffect {

    public BecomePreparedEffect() {
        this(null);
    }

    @Override
    public CardEffect withGrantingPermanentId(UUID permanentId) {
        return new BecomePreparedEffect(permanentId);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
