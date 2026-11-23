package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "124")
public class ElvishSoultiller extends Card {

    public ElvishSoultiller() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ChooseCreatureTypeAtResolutionEffect(),
                new ShuffleGraveyardIntoLibraryEffect(false, new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE), new CardHasTypeChosenThisResolutionPredicate()))));
    }
}
