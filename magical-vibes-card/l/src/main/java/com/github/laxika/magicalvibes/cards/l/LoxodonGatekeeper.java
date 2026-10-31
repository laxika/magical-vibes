package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;

import java.util.Set;

@CardRegistration(set = "RAV", collectorNumber = "25")
@CardRegistration(set = "SLD", collectorNumber = "2354")
@CardRegistration(set = "SLD", collectorNumber = "2358")
public class LoxodonGatekeeper extends Card {

    public LoxodonGatekeeper() {
        addStaticEffect(new EnterPermanentsOfTypesTappedEffect(
                Set.of(CardType.ARTIFACT, CardType.CREATURE, CardType.LAND), true));
    }
}
