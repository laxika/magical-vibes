package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawThreeIfFewerThanThreeCardsDiscardedEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsDownToHandSizeEffect;

@CardRegistration(set = "DSC", collectorNumber = "365")
public class YourNightmaresAreDelicious extends Card {

    public YourNightmaresAreDelicious() {
        addSpellEffect(new EachOpponentDiscardsDownToHandSizeEffect(
                5, new DrawThreeIfFewerThanThreeCardsDiscardedEffect()));
    }
}
