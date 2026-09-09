package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "SOS", collectorNumber = "158")
public class PlanarEngineering extends Card {

    public PlanarEngineering() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                2, new PermanentIsLandPredicate(), SacrificeRecipient.CONTROLLER));
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new Fixed(4), CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }
}
