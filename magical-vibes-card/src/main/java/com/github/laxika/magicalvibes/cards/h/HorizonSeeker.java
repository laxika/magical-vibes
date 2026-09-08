package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "175")
public class HorizonSeeker extends Card {

    public HorizonSeeker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "Boast — {1}{G}: Search your library for a basic land card, reveal it, put it into your hand, then shuffle. Activate only if this creature attacked this turn and only once each turn.",
                1
        ).withActivationCondition(
                new NotCondition(new DidntAttack()),
                "Activate only if this creature attacked this turn."
        ).withBoast());
    }
}
