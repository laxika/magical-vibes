package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GargantuanSlabhorn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryMatchesPredicate;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsDoubleFacedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "240")
public class InvasionOfPyrulea extends Card {

    public InvasionOfPyrulea() {
        setBackFaceCard(new GargantuanSlabhorn());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(3));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new TopCardOfLibraryMatchesPredicate(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.LAND),
                        new CardIsDoubleFacedPredicate()))),
                new DrawCardEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "GargantuanSlabhorn";
    }
}
