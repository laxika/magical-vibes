package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.RevealCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.condition.ControllerHasNoLandCardsInHand;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "255")
public class LandGrant extends Card {

    public LandGrant() {
        addCastingOption(new AlternateHandCast(
                List.of(RevealCardsFromHandCastingCost.entireHand()),
                new ControllerHasNoLandCardsInHand(),
                false));
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardSubtypePredicate(CardSubtype.FOREST)));
    }
}
