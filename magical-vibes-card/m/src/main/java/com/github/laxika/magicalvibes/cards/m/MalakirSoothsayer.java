package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "OGW", collectorNumber = "87")
public class MalakirSoothsayer extends Card {

    public MalakirSoothsayer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TapCreatureCost(new PermanentHasSubtypePredicate(CardSubtype.ALLY), true, false),
                        new DrawCardEffect(1),
                        new LoseLifeEffect(1, LoseLifeRecipient.CONTROLLER)
                ),
                "{T}, Tap an untapped Ally you control: You draw a card and you lose 1 life."
        ));
    }
}
