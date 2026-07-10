package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "210")
public class RathiDragon extends Card {

    public RathiDragon() {
        // Flying is auto-loaded from Scryfall.
        // When Rathi Dragon enters, sacrifice it unless you sacrifice two Mountains.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ForcedCostOrElseEffect(
                new SacrificeMultiplePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN)),
                List.of(new SacrificeSelfEffect()),
                true));
    }
}
