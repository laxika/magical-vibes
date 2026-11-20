package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "245")
@CardRegistration(set = "MOM", collectorNumber = "312")
public class KroxaAndKunoros extends Card {

    public KroxaAndKunoros() {
        ReturnCardFromGraveyardEffect returnCreature = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build();

        MayEffect exileFive = new MayEffect(
                SequenceEffect.of(
                        ExileGraveyardCardsEffect.fromOwnGraveyard(5, null),
                        new ConditionalEffect(HappenedThisWay.atLeast(StepKind.EXILE, 5),
                                new QueueReflexiveAbilityEffect(returnCreature))),
                "Exile five cards from your graveyard?");
        addTrigger(Triggers.enters(ObjectRelation.THIS), exileFive);
        addTrigger(Triggers.attacks(), exileFive);
    }
}
