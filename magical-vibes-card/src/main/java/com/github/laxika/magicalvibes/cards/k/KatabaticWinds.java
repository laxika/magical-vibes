package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingPermanentsCantActivateTapAbilitiesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

/**
 * Katabatic Winds — {2}{G} Enchantment.
 * "Phasing"
 * "Creatures with flying can't attack or block, and their activated abilities with {T} in their
 * costs can't be activated."
 *
 * <p>Phasing is a printed keyword loaded from Scryfall; PhasingService handles it.
 */
@CardRegistration(set = "VIS", collectorNumber = "109")
public class KatabaticWinds extends Card {

    public KatabaticWinds() {
        PermanentHasKeywordPredicate flying = new PermanentHasKeywordPredicate(Keyword.FLYING);
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCantAttackOrBlockEffect(
                flying, "Creatures with flying can't attack or block"));
        addEffect(EffectSlot.STATIC, new MatchingPermanentsCantActivateTapAbilitiesEffect(flying));
    }
}
