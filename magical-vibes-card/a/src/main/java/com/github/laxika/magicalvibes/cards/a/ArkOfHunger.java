package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayPlayFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "173")
@CardRegistration(set = "SOS", collectorNumber = "344")
public class ArkOfHunger extends Card {

    public ArkOfHunger() {
        // Whenever one or more cards leave your graveyard, this artifact deals 1 damage
        // to each opponent and you gain 1 life.
        addTrigger(new TriggerCondition(Triggers.leavesGraveyard(null, ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE, SequenceEffect.of(
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT), new GainLifeEffect(1)));

        // {T}: Mill a card. You may play that card this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new MillControllerAndMayPlayFromGraveyardThisTurnEffect()),
                "{T}: Mill a card. You may play that card this turn."
        ));
    }
}
