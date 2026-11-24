package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "SOM", collectorNumber = "72")
@CardRegistration(set = "SLD", collectorNumber = "133")
@CardRegistration(set = "2X2", collectorNumber = "83")
public class NecroticOoze extends Card {

    public NecroticOoze() {
        addStaticEffect(new GainActivatedAbilitiesOfExiledCardsEffect(
                Subjects.cardsInGraveyards(Subjects.eachPlayer(), new CardTypePredicate(CardType.CREATURE))));
    }
}
