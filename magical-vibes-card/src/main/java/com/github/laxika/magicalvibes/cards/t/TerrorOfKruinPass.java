package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

/**
 * Terror of Kruin Pass — back face of Kruin Outlaw.
 * 3/3 Werewolf with Double strike.
 * Werewolves you control have menace.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Terror of Kruin Pass.
 */
public class TerrorOfKruinPass extends Card {

    public TerrorOfKruinPass() {
        // Double strike is loaded from Scryfall.

        // Werewolves you control have menace (including self).
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 0, Set.of(Keyword.MENACE), GrantScope.ALL_OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WEREWOLF))));

        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Terror of Kruin Pass.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new TwoOrMoreSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }
}
