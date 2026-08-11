package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTurnFaceUpEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "KTK", collectorNumber = "136")
public class HoodedHydra extends Card {

    public HoodedHydra() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addEffect(EffectSlot.ON_DEATH, new CreateTokensForEachDyingSourceCounterEffect(
                CounterType.PLUS_ONE_PLUS_ONE,
                new CreateTokenEffect("Snake", 1, 1, CardColor.GREEN,
                        List.of(CardSubtype.SNAKE), Set.of(), Set.of())));
        addMorph("{3}{G}{G}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new PutCountersOnTurnFaceUpEffect(5));
    }
}
