package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

/**
 * Vildin-Pack Alpha — back face of Geier Reach Bandit.
 * 4/3 Werewolf.
 * Whenever a Werewolf you control enters, you may transform it.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform this creature.
 */
public class VildinPackAlpha extends Card {

    public VildinPackAlpha() {
        // Whenever a Werewolf you control enters, you may transform it.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.WEREWOLF),
                        new TransformEnteringCreatureEffect()));

        // At the beginning of each upkeep, if a player cast two or more spells last turn,
        // transform Vildin-Pack Alpha.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new TransformSelfEffect()));
    }
}
