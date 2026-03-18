package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

/**
 * Wildblood Pack — back face of Instigator Gang.
 * 5/5 Werewolf with Trample.
 * Attacking creatures you control get +3/+0.
 * At the beginning of each upkeep, if a player cast two or more spells last turn, transform Wildblood Pack.
 */
public class WildbloodPack extends Card {

    public WildbloodPack() {
        // Attacking creatures you control get +3/+0.
        // OWN_CREATURES handles other creatures; SELF handles the source itself (no "other" in oracle text).
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(3, 0, GrantScope.OWN_CREATURES, new PermanentIsAttackingPredicate()));
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(3, 0, GrantScope.SELF, new PermanentIsAttackingPredicate()));

        // At the beginning of each upkeep, if a player cast two or more spells last turn, transform Wildblood Pack.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new TwoOrMoreSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }
}
