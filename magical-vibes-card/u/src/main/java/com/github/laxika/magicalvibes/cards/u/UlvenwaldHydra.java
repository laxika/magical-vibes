package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "SOI", collectorNumber = "235")
@CardRegistration(set = "SIR", collectorNumber = "222")
@CardRegistration(set = "M3C", collectorNumber = "250")
public class UlvenwaldHydra extends Card {

    public UlvenwaldHydra() {
        PermanentCount landsYouControl = new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(landsYouControl, landsYouControl));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SearchLibraryEffect(new CardTypePredicate(CardType.LAND),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                        "Search your library for a land card?"));
    }
}
