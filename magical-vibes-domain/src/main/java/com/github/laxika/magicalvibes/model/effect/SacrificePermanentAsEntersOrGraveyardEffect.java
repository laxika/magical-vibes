package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "If this permanent would enter, sacrifice a [permanent] instead. If you do, put it onto the
 * battlefield. If you don't, put it into its owner's graveyard." (Alliances trading-post land
 * cycle, e.g. Balduvian Trading Post.)
 * <p>
 * Replacement effect (CR 614) registered in {@code EffectSlot.STATIC} and applied by
 * {@code BattlefieldEntryService} before the permanent is placed. The controller chooses which
 * matching permanents to sacrifice, and may decline (the card then goes to its owner's graveyard);
 * with fewer than the required number of matching permanents there is no prompt and the card goes
 * straight to the graveyard.
 *
 * @param filter      which permanents the controller may sacrifice (e.g. untapped Mountain)
 * @param count       how many matching permanents must be sacrificed for the permanent to enter
 * @param description human-readable name of the sacrifice, used in the prompt
 */
public record SacrificePermanentAsEntersOrGraveyardEffect(PermanentPredicate filter, int count, String description)
        implements EntryCostReplacementEffect {

    public SacrificePermanentAsEntersOrGraveyardEffect(PermanentPredicate filter, String description) {
        this(filter, 1, description);
    }

    public SacrificePermanentAsEntersOrGraveyardEffect {
        if (count < 1) {
            throw new IllegalArgumentException("count must be positive");
        }
    }

    @Override
    public Kind kind() {
        return Kind.SACRIFICE_PERMANENT;
    }

    @Override
    public PermanentPredicate permanentFilter() {
        return filter;
    }
}
