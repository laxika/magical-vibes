package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.condition.TwoOrMoreSpellsCastLastTurn;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Moonscarred Werewolf — back face of Scorned Villager.
 * 2/2 Werewolf with vigilance.
 * {T}: Add {G}{G}.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Moonscarred Werewolf.
 */
public class MoonscarredWerewolf extends Card {

    public MoonscarredWerewolf() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN, 2));

        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new TwoOrMoreSpellsCastLastTurn()),
                new TransformSelfEffect());
    }
}
