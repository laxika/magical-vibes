package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyRandomAttackingCreatureEffect;

import java.util.List;

public class MetzaliTowerOfTriumph extends Card {

    public MetzaliTowerOfTriumph() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(new DealDamageToPlayersEffect(2, DamageRecipient.EACH_OPPONENT)),
                "{1}{R}, {T}: Metzali deals 2 damage to each opponent."
        ));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{W}",
                List.of(new DestroyRandomAttackingCreatureEffect()),
                "{2}{W}, {T}: Choose a creature at random that attacked this turn. Destroy that creature."
        ));
    }
}
