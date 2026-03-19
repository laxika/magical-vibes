package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;

import java.util.List;

/**
 * Ulvenwald Primordials — back face of Ulvenwald Mystics.
 * 5/5 Werewolf.
 * {G}: Regenerate Ulvenwald Primordials.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Ulvenwald Primordials.
 */
public class UlvenwaldPrimordials extends Card {

    public UlvenwaldPrimordials() {
        // {G}: Regenerate Ulvenwald Primordials.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new RegenerateEffect()),
                "{G}: Regenerate Ulvenwald Primordials."
        ));

        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Ulvenwald Primordials.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new TwoOrMoreSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }
}
