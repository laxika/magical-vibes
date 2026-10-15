package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "8")
@CardRegistration(set = "40K", collectorNumber = "175")
public class MarneusCalgar extends Card {

    public MarneusCalgar() {
        addEffect(EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD, new DrawCardEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}",
                List.of(new CreateTokenEffect(
                        2, "Astartes Warrior", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.ASTARTES, CardSubtype.WARRIOR),
                        Set.of(Keyword.VIGILANCE), Set.of())),
                "{6}: Create two 2/2 white Astartes Warrior creature tokens with vigilance."
        ));
    }
}
