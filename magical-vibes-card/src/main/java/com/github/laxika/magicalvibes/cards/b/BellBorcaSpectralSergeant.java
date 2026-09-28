package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueNotedForSourceThisTurn;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.NoteManaValueOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "DMC", collectorNumber = "145")
public class BellBorcaSpectralSergeant extends Card {

    public BellBorcaSpectralSergeant() {
        addEffect(EffectSlot.ON_ANY_CARD_EXILED, new NoteManaValueOfExiledCardsEffect());
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                new GreatestManaValueNotedForSourceThisTurn(), new Fixed(5)));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ExileTopCardMayPlayThisTurnEffect(false));
    }
}
