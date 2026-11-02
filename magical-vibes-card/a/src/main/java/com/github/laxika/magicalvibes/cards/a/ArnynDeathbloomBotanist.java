package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "74")
public class ArnynDeathbloomBotanist extends Card {

    private static final PermanentPredicate POWER_OR_TOUGHNESS_ONE_OR_LESS = new PermanentAnyOfPredicate(List.of(
            new PermanentPowerAtMostPredicate(1),
            new PermanentToughnessAtMostPredicate(1)
    ));

    private static final SequenceEffect DRAIN =
            SequenceEffect.of(
                    new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER),
                    new GainLifeEffect(2));

    private static final CardEffect CONDITIONAL_DRAIN =
            DRAIN;

    public ArnynDeathbloomBotanist() {
        // Deathtouch is applied automatically from the Scryfall keyword.

        // Whenever a creature you control with power or toughness 1 or less dies,
        // target opponent loses 2 life and you gain 2 life.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.creatureDies(POWER_OR_TOUGHNESS_ONE_OR_LESS, ObjectRelation.YOU_CONTROL),
                CONDITIONAL_DRAIN);
    }
}
