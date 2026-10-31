package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "KLD", collectorNumber = "49")
@CardRegistration(set = "KLR", collectorNumber = "49")
@CardRegistration(set = "M3C", collectorNumber = "187")
@CardRegistration(set = "PIP", collectorNumber = "176")
@CardRegistration(set = "PIP", collectorNumber = "704")
@CardRegistration(set = "DRC", collectorNumber = "78")
public class GlimmerOfGenius extends Card {

    public GlimmerOfGenius() {
        addSpellEffect(new ScryEffect(2));
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new EnergyCountersEffect(2));
    }
}
