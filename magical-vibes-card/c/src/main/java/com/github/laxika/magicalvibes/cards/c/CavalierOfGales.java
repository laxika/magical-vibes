package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.HandToLibraryPlacement;
import com.github.laxika.magicalvibes.model.effect.DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "52")
public class CavalierOfGales extends Card {

    public CavalierOfGales() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DrawThenPutCardsFromHandOnTopOrBottomOfLibraryEffect(3, 2, HandToLibraryPlacement.TOP));
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new ShuffleSelfFromGraveyardIntoLibraryEffect(),
                new ScryEffect(2)));
    }
}
