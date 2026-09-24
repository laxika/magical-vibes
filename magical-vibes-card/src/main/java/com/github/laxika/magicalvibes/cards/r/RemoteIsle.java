package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "USG", collectorNumber = "324")
@CardRegistration(set = "BRB", collectorNumber = "58")
@CardRegistration(set = "BTD", collectorNumber = "75")
@CardRegistration(set = "SLD", collectorNumber = "2162")
@CardRegistration(set = "DMR", collectorNumber = "254")
@CardRegistration(set = "C14", collectorNumber = "309")
public class RemoteIsle extends Card {

    public RemoteIsle() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
        addCycling("{2}");
    }
}
