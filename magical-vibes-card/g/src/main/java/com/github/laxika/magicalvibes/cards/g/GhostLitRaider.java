package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "99")
public class GhostLitRaider extends Card {

    public GhostLitRaider() {
        addActivatedAbility(new ActivatedAbility(true, "{2}{R}",
                List.of(DealDamageEffect.toTargetCreature(2)),
                "{2}{R}, {T}: This creature deals 2 damage to target creature."));

        addActivatedAbility(AbilityZones.HAND, new ActivatedAbility(false, "{3}{R}",
                List.of(DealDamageEffect.toTargetCreature(4)),
                "Channel — {3}{R}, Discard this card: It deals 4 damage to target creature."));
    }
}
