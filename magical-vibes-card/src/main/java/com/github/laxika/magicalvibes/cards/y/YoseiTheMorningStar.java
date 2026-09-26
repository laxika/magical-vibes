package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.effect.SkipRecipient;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

/**
 * Yosei, the Morning Star — "When Yosei dies, target player skips their next untap step. Tap up to
 * five target permanents that player controls."
 *
 * <p>The player and up to five permanents are chosen as targets before the single death trigger
 * goes on the stack. A {@link SequenceEffect} keeps the skip and tap in one trigger.
 */
@CardRegistration(set = "CHK", collectorNumber = "50")
@CardRegistration(set = "MMA", collectorNumber = "35")
@CardRegistration(set = "IMA", collectorNumber = "39")
public class YoseiTheMorningStar extends Card {

    public YoseiTheMorningStar() {
        SequenceEffect trigger = SequenceEffect.of(
                new SkipNextEffect(SkipKind.UNTAP_STEP, SkipRecipient.TARGET_PLAYER),
                new TapPermanentsEffect(TapUntapScope.TARGET));
        target(new PlayerPredicateTargetFilter(new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"));
        target(new PermanentPredicateTargetFilter(new PermanentTruePredicate(),
                "Target must be a permanent"), 0, 5).addEffect(EffectSlot.ON_DEATH, trigger);
        setMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET);
    }
}
