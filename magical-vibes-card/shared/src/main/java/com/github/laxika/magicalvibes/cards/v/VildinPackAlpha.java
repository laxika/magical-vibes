package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TransformEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Vildin-Pack Alpha — back face of Geier Reach Bandit.
 * 4/3 Werewolf.
 * Whenever a Werewolf you control enters, you may transform it.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform this creature.
 */
public class VildinPackAlpha extends Card {

    public VildinPackAlpha() {
        // Whenever a Werewolf you control enters, you may transform it.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.WEREWOLF),
                        new TransformEnteringCreatureEffect()));

        // At the beginning of each upkeep, if a player cast two or more spells last turn,
        // transform Vildin-Pack Alpha.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new TransformSelfEffect()));
    }
}
