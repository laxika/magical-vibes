package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffect;

import java.util.List;

@CardRegistration(set = "YNEO", collectorNumber = "7")
public class HolographicDouble extends Card {

    public HolographicDouble() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffect()),
                "{U}, Exile Holographic Double from your hand: Choose a creature card in your hand. "
                        + "Conjure a duplicate of it into your hand."
        ).withExilesSourceFromHand());
    }
}
