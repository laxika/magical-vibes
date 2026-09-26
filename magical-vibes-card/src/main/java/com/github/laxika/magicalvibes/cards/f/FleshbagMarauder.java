package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ALA", collectorNumber = "76")
@CardRegistration(set = "ORI", collectorNumber = "98")
@CardRegistration(set = "DDD", collectorNumber = "38")
@CardRegistration(set = "DDN", collectorNumber = "8")
@CardRegistration(set = "GVL", collectorNumber = "38")
@CardRegistration(set = "SLD", collectorNumber = "725")
@CardRegistration(set = "SLD", collectorNumber = "841")
@CardRegistration(set = "SLD", collectorNumber = "1175")
@CardRegistration(set = "GNT", collectorNumber = "29")
@CardRegistration(set = "GN3", collectorNumber = "50")
@CardRegistration(set = "CMD", collectorNumber = "83")
public class FleshbagMarauder extends Card {

    public FleshbagMarauder() {
        // When this creature enters, each player sacrifices a creature of their choice.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_PLAYER));
    }
}
