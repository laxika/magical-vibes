package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "371")
@CardRegistration(set = "CMM", collectorNumber = "600")
public class Boompile extends Card {

    public Boompile() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new FlipCoinWinEffect(
                        new DestroyAllPermanentsEffect(new PermanentNotPredicate(new PermanentIsLandPredicate())))),
                "{T}: Flip a coin. If you win the flip, destroy all nonland permanents."
        ));
    }
}
