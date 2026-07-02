package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

import java.util.List;

/**
 * Krallenhorde Killer — back face of Wolfbitten Captive.
 * 2/2 Werewolf.
 * {3}{G}: This creature gets +4/+4 until end of turn. Activate only once each turn.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform this creature.
 */
public class KrallenhordeKiller extends Card {

    public KrallenhordeKiller() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new BoostSelfEffect(4, 4)),
                "{3}{G}: This creature gets +4/+4 until end of turn. Activate only once each turn.",
                1
        ));

        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new ConditionalEffect(new TwoOrMoreSpellsCastLastTurn(), new TransformSelfEffect()));
    }
}
