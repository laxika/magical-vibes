package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "If this permanent would enter, sacrifice a [permanent] instead. If you do, put it onto the
 * battlefield. If you don't, put it into its owner's graveyard." (Alliances trading-post land
 * cycle, e.g. Balduvian Trading Post.)
 * <p>
 * Replacement effect (CR 614) registered in {@code EffectSlot.STATIC} and applied by
 * {@code BattlefieldEntryService} before the permanent is placed. The controller chooses which
 * matching permanent to sacrifice, and may decline (the card then goes to its owner's graveyard);
 * with no matching permanent there is no prompt and the card goes straight to the graveyard.
 *
 * @param filter      which permanents the controller may sacrifice (e.g. untapped Mountain)
 * @param description human-readable name of the sacrifice, used in the prompt
 */
public record SacrificePermanentAsEntersOrGraveyardEffect(PermanentPredicate filter, String description)
        implements ReplacementEffect {
}
