package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "242")
public class ZoralineCosmosCaller extends Card {

    public ZoralineCosmosCaller() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.BAT),
                        new GainLifeEffect(1)));

        var returnPermanent = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardAllOfPredicate(List.of(
                        new CardIsPermanentPredicate(),
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        new CardMaxManaValuePredicate(3))))
                .targetGraveyard(true)
                .enterWithCounter(CounterType.FINALITY)
                .enterWithCounterCount(1)
                .build();
        var mayReturnPermanent = new MayPayManaEffect(
                "{W}{B}",
                2,
                returnPermanent,
                "Pay {W}{B} and 2 life to return target nonland permanent card with mana value 3 or less from your graveyard to the battlefield with a finality counter on it?");
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, mayReturnPermanent);
        addEffect(EffectSlot.ON_ATTACK, mayReturnPermanent);
    }
}
