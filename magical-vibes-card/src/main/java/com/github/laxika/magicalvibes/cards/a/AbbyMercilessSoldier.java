package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "2202")
public class AbbyMercilessSoldier extends Card {

    public AbbyMercilessSoldier() {
        addEffect(EffectSlot.ON_SELF_CAST, new CreateTokenEffect(
                new ManaSpentToCast(), "Cordyceps Infected", 1, 1, CardColor.BLACK,
                List.of(CardSubtype.FUNGUS, CardSubtype.ZOMBIE), Set.of(), Set.of()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseOpponentGainsControlOfSourceEffect());
    }
}
