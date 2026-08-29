package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ChosenNumberOnSource;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfLifeOnEnterEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "306")
public class PhyrexianProcessor extends Card {

    public PhyrexianProcessor() {
        // As this artifact enters, pay any amount of life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PayAnyAmountOfLifeOnEnterEffect());

        // {4}, {T}: Create an X/X black Phyrexian Minion creature token, where X is the life paid
        // as this artifact entered.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new CreateTokenEffect(
                        "Phyrexian Minion",
                        new ChosenNumberOnSource(),
                        new ChosenNumberOnSource(),
                        CardColor.BLACK,
                        List.of(CardSubtype.PHYREXIAN, CardSubtype.MINION),
                        Set.of(),
                        Set.of())),
                "{4}, {T}: Create an X/X black Phyrexian Minion creature token, where X is the life paid as this artifact entered."));
    }
}
