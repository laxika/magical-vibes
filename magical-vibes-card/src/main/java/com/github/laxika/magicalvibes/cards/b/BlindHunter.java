package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GPT", collectorNumber = "102")
public class BlindHunter extends Card {

    public BlindHunter() {
        target(anyPlayer())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2));
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_DEATH, new HauntEffect());
        target(anyPlayer())
                .addEffect(EffectSlot.ON_HAUNTED_CREATURE_DIES,
                        new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER))
                .addEffect(EffectSlot.ON_HAUNTED_CREATURE_DIES, new GainLifeEffect(2));
    }

    private static PlayerPredicateTargetFilter anyPlayer() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player");
    }
}
