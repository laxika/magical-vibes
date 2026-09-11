package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ALL", collectorNumber = "12a")
@CardRegistration(set = "ALL", collectorNumber = "12b")
@CardRegistration(set = "CST", collectorNumber = "5")
@CardRegistration(set = "CST", collectorNumber = "12b")
@CardRegistration(set = "ME2", collectorNumber = "28")
public class Reinforcements extends Card {

    public Reinforcements() {
        addEffect(EffectSlot.SPELL,
                new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(new CardTypePredicate(CardType.CREATURE), 3));
    }
}
