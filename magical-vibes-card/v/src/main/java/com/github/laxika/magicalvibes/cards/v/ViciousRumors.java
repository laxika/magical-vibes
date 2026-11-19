package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "GRN", collectorNumber = "89")
public class ViciousRumors extends Card {

    public ViciousRumors() {
        addSpellEffect(new DealDamageToPlayersEffect(1, Subjects.eachOpponent()));
        addSpellEffect(new DiscardEffect(1, Subjects.eachOpponent()));
        addSpellEffect(new MillEffect(1, Subjects.eachOpponent()));
        addSpellEffect(new GainLifeEffect(1));
    }
}
