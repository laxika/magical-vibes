package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEachPlayerAndPutMilledCardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "DSC", collectorNumber = "364")
public class YourMistakeIsMyTriumph extends Card {

    public YourMistakeIsMyTriumph() {
        addEffect(EffectSlot.SPELL,
                new MillEachPlayerAndPutMilledCardOntoBattlefieldEffect(
                        3, new CardIsPermanentPredicate()));
    }
}
