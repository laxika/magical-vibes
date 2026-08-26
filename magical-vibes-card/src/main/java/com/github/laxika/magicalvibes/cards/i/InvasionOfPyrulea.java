package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GargantuanSlabhorn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryMatchesPredicate;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsDoubleFacedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "240")
public class InvasionOfPyrulea extends Card {

    public InvasionOfPyrulea() {
        setBackFaceCard(new GargantuanSlabhorn());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(3));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
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
