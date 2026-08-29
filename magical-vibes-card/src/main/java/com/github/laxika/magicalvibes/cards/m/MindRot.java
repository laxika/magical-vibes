package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "159")
@CardRegistration(set = "M10", collectorNumber = "105")
@CardRegistration(set = "M11", collectorNumber = "105")
@CardRegistration(set = "M12", collectorNumber = "101")
@CardRegistration(set = "M13", collectorNumber = "100")
@CardRegistration(set = "M14", collectorNumber = "106")
@CardRegistration(set = "9ED", collectorNumber = "145")
@CardRegistration(set = "POR", collectorNumber = "101")
@CardRegistration(set = "P02", collectorNumber = "78")
@CardRegistration(set = "8ED", collectorNumber = "144")
@CardRegistration(set = "7ED", collectorNumber = "147")
@CardRegistration(set = "RTR", collectorNumber = "70")
@CardRegistration(set = "M15", collectorNumber = "104")
@CardRegistration(set = "M19", collectorNumber = "109")
@CardRegistration(set = "M20", collectorNumber = "108")
@CardRegistration(set = "KLD", collectorNumber = "93")
@CardRegistration(set = "S99", collectorNumber = "83")
@CardRegistration(set = "M21", collectorNumber = "115")
public class MindRot extends Card {

    public MindRot() {
        addEffect(EffectSlot.SPELL, new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER));
    }
}
