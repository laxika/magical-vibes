package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;

/**
 * Moonscarred Werewolf — back face of Scorned Villager.
 * 2/2 Werewolf with vigilance.
 * {T}: Add {G}{G}.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Moonscarred Werewolf.
 */
public class MoonscarredWerewolf extends Card {

    public MoonscarredWerewolf() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN, 2));

        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new TwoOrMoreSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }
}
