package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RippleEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "20")
public class SurgingSentinels extends Card {

    public SurgingSentinels() {
        addTrigger(Triggers.youCastThisSpell(),
                new MayEffect(new RippleEffect(4), "Reveal the top four cards of your library?"));
    }
}
