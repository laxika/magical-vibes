package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.effect.SeekToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YEOE", collectorNumber = "30")
public class SpiritedSimulacrum extends Card {

    public SpiritedSimulacrum() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SeekToBattlefieldEffect(new CardTypePredicate(CardType.LAND)));
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new SeekEffect(new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}"))));
    }
}
