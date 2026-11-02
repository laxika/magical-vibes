package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "15")
@CardRegistration(set = "DDC", collectorNumber = "12")
@CardRegistration(set = "DVD", collectorNumber = "12")
public class LuminousAngel extends Card {

    public LuminousAngel() {
        // At the beginning of your upkeep, you may create a 1/1 white Spirit creature token with flying.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                CreateTokenEffect.whiteSpirit(1),
                "Create a 1/1 white Spirit creature token with flying?"));
    }
}
