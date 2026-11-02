package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MBS", collectorNumber = "34")
@CardRegistration(set = "DDU", collectorNumber = "40")
@CardRegistration(set = "2XM", collectorNumber = "73")
public class TreasureMage extends Card {

    public TreasureMage() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new SearchLibraryEffect(
                        new CardAllOfPredicate(List.of(new CardTypePredicate(CardType.ARTIFACT), new CardMinManaValuePredicate(6)))),
                        "Search your library for an artifact card with mana value 6 or greater?"));
    }
}
