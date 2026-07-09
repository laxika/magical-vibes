package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AshlingThePilgrimEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "149")
public class AshlingThePilgrim extends Card {

    public AshlingThePilgrim() {
        // {1}{R}: Put a +1/+1 counter on Ashling the Pilgrim. If this is the third time this ability
        // has resolved this turn, remove all +1/+1 counters from Ashling the Pilgrim, and it deals
        // that much damage to each creature and each player.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new AshlingThePilgrimEffect()),
                "{1}{R}: Put a +1/+1 counter on Ashling the Pilgrim. If this is the third time this ability has resolved this turn, remove all +1/+1 counters from Ashling the Pilgrim, and it deals that much damage to each creature and each player."
        ));
    }
}
