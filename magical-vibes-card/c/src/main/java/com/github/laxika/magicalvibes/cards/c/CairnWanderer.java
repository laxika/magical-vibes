package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

@CardRegistration(set = "LRW", collectorNumber = "105")
@CardRegistration(set = "C20", collectorNumber = "130")
public class CairnWanderer extends Card {

    public CairnWanderer() {
        addStaticEffect(new GainKeywordsOfCreatureCardsExiledWithSourceEffect(
                Subjects.cardsInGraveyards(Subjects.eachPlayer(), new CardTypePredicate(CardType.CREATURE)),
                Set.of(Keyword.FLYING, Keyword.FEAR, Keyword.FIRST_STRIKE, Keyword.DOUBLE_STRIKE, Keyword.DEATHTOUCH,
                        Keyword.HASTE, Keyword.FORESTWALK, Keyword.MOUNTAINWALK, Keyword.ISLANDWALK, Keyword.SWAMPWALK,
                        Keyword.PLAINSWALK, Keyword.DESERTWALK, Keyword.LIFELINK, Keyword.REACH, Keyword.TRAMPLE,
                        Keyword.SHROUD, Keyword.VIGILANCE),
                true));
    }
}
