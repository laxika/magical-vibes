package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "126")
public class ArmsRace extends Card {

    public ArmsRace() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new MayEffect(
                        new PutCardToBattlefieldEffect(
                                new CardTypePredicate(CardType.ARTIFACT),
                                "artifact", false, false, true, true),
                        "Put an artifact card from your hand onto the battlefield?"
                )),
                "{3}{R}: You may put an artifact card from your hand onto the battlefield. "
                        + "That artifact gains haste. Sacrifice it at the beginning of the next end step."
        ));
    }
}
