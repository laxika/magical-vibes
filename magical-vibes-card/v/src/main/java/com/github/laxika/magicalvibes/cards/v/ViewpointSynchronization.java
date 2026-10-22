package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Freerunning;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "43")
public class ViewpointSynchronization extends Card {

    public ViewpointSynchronization() {
        addCastingOption(new AlternateHandCast(
                List.of(new ManaCastingCost("{2}{G}")), new Freerunning(), false));
        addEffect(EffectSlot.SPELL,
                SearchLibraryForBasicLandsToBattlefieldTappedAndHandEffect.twoToBattlefieldTapped());
    }
}
