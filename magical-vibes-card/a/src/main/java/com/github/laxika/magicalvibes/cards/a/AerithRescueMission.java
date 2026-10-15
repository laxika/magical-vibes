package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnOneTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "5")
public class AerithRescueMission extends Card {

    public AerithRescueMission() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Take the Elevator",
                        new CreateTokenEffect(3, "Hero", 1, 1, null, List.of(CardSubtype.HERO), Set.of(), Set.of())),
                new ChooseOneEffect.ChooseOneOption(
                        "Take 59 Flights of Stairs",
                        List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                        TargetFilters.creature(), null, 0, 3, false, null)
        )));
        addEffect(EffectSlot.SPELL, new PutCounterOnOneTargetEffect(CounterType.STUN));
    }
}
