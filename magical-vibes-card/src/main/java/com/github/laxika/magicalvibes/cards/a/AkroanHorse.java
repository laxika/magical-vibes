package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentCreatesTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "210")
public class AkroanHorse extends Card {

    public AkroanHorse() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOpponentGainsControlOfSourceEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new EachOpponentCreatesTokenEffect(
                new CreateTokenEffect("Soldier", 1, 1, CardColor.WHITE, List.of(CardSubtype.SOLDIER), Set.of(), Set.of())));
    }
}
