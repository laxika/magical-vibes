package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "GRN", collectorNumber = "8")
public class DawnOfHope extends Card {

    public DawnOfHope() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE,
                new MayPayManaEffect("{2}", new DrawCardEffect(1), "Pay {2} to draw a card?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}",
                List.of(new CreateTokenEffect(
                        1, "Soldier", 1, 1, CardColor.WHITE,
                        List.of(CardSubtype.SOLDIER), Set.of(Keyword.LIFELINK), Set.of())),
                "{3}{W}: Create a 1/1 white Soldier creature token with lifelink."
        ));
    }
}
