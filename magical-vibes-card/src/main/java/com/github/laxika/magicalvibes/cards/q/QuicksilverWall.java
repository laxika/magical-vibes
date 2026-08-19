package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

import java.util.List;

@CardRegistration(set = "PCY", collectorNumber = "41")
public class QuicksilverWall extends Card {

    public QuicksilverWall() {
        addActivatedAbility(new ActivatedAbility(false, "{4}", List.of(ReturnToHandEffect.self()),
                "{4}: Return this creature to its owner's hand. Any player may activate this ability.")
                .withActivatableByAnyPlayer());
    }
}
