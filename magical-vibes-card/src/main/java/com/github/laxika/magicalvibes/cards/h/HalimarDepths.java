package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;

@CardRegistration(set = "WWK", collectorNumber = "137")
@CardRegistration(set = "DDM", collectorNumber = "36")
@CardRegistration(set = "SLD", collectorNumber = "2159")
@CardRegistration(set = "DSC", collectorNumber = "282")
@CardRegistration(set = "AFC", collectorNumber = "244")
@CardRegistration(set = "C20", collectorNumber = "280")
@CardRegistration(set = "C18", collectorNumber = "253")
public class HalimarDepths extends Card {

    public HalimarDepths() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReorderTopCardsOfLibraryEffect(3));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
