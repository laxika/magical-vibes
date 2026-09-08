package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "166")
public class ThrashingWumpus extends Card {

    public ThrashingWumpus() {
        addActivatedAbility(new ActivatedAbility(false, "{B}",
                List.of(new MassDamageEffect(1, true)),
                "{B}: This creature deals 1 damage to each creature and each player."));
    }
}
