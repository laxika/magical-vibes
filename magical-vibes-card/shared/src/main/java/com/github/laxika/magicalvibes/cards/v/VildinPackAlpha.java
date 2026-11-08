package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TransformTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * Vildin-Pack Alpha — back face of Geier Reach Bandit.
 * 4/3 Werewolf.
 * Whenever a Werewolf you control enters, you may transform it.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform this creature.
 */
public class VildinPackAlpha extends Card {

    public VildinPackAlpha() {
        // Whenever a Werewolf you control enters, you may transform it.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.WEREWOLF))),
                        ObjectRelation.YOU_CONTROL),
                new MayEffect(TransformTargetPermanentEffect.ofTriggeringPermanent(), "Transform it?"));

        // At the beginning of each upkeep, if a player cast two or more spells last turn,
        // transform Vildin-Pack Alpha.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new TwoOrMoreSpellsCastLastTurn()),
                new TransformSelfEffect());
    }
}
