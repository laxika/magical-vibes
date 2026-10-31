package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "TSP", collectorNumber = "44")
public class TivadarOfThorn extends Card {

    public TivadarOfThorn() {
        // Protection from red
        addStaticEffect(new ProtectionFromColorsEffect(Set.of(CardColor.RED)));

        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                "Target must be a Goblin"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect());
    }
}
