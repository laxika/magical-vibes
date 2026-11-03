package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new TwoOrMoreSpellsCastLastTurn()),
                new TransformSelfEffect());
    }
}
