package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SavagePackmate;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "234")
public class ChildOfThePack extends Card {

    public ChildOfThePack() {
        setBackFaceCard(new SavagePackmate());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}{G}",
                List.of(new CreateTokenEffect(
                        "Wolf", 2, 2, CardColor.GREEN, List.of(CardSubtype.WOLF), Set.of(), Set.of())),
                "{2}{R}{G}: Create a 2/2 green Wolf creature token."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "SavagePackmate";
    }
}
