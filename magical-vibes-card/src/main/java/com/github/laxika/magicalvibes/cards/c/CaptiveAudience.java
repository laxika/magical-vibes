package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatesTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "160")
public class CaptiveAudience extends Card {

    public CaptiveAudience() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOpponentGainsControlOfSourceEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ChooseModeNotYetChosenEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Your life total becomes 4",
                        new SetLifeTotalEffect(4)),
                new ChooseOneEffect.ChooseOneOption(
                        "Discard your hand",
                        new DiscardHandEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent creates five 2/2 black Zombie creature tokens",
                        new EachOpponentCreatesTokenEffect(CreateTokenEffect.blackZombie(5))))));
    }
}
