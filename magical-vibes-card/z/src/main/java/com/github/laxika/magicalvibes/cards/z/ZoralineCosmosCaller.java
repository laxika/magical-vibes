package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "242")
public class ZoralineCosmosCaller extends Card {

    public ZoralineCosmosCaller() {
        addTrigger(Triggers.creatureYouControlAttacks() .withFilter(new PermanentHasSubtypePredicate(CardSubtype.BAT)),
                new GainLifeEffect(1));

        var returnPermanent = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardAllOfPredicate(List.of(
                        new CardIsPermanentPredicate(),
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        new CardMaxManaValuePredicate(3))))
                .targetGraveyard(true)
                .chooseAuraAttachment(true)
                .enterWithCounter(CounterType.FINALITY)
                .enterWithCounterCount(1)
                .build();
        var mayReturnPermanent = new MayPayManaEffect(
                "{W}{B}",
                2,
                new QueueReflexiveAbilityEffect(returnPermanent),
                "Pay {W}{B} and 2 life to return target nonland permanent card with mana value 3 or less from your graveyard to the battlefield with a finality counter on it?");
        addTrigger(Triggers.enters(ObjectRelation.THIS), mayReturnPermanent);
        addTrigger(Triggers.attacks(), mayReturnPermanent);
    }
}
