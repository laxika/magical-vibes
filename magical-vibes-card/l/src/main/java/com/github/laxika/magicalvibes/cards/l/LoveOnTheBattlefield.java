package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ExactlyAttackers;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "MSC", collectorNumber = "87")
@CardRegistration(set = "MSC", collectorNumber = "408")
public class LoveOnTheBattlefield extends Card {

    public LoveOnTheBattlefield() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(
                        new ExactlyAttackers(2),
                        SequenceEffect.of(
                                new GrantKeywordEffect(
                                        Keyword.FIRST_STRIKE,
                                        GrantScope.OWN_CREATURES,
                                        new PermanentIsAttackingPredicate()),
                                new DrawCardEffect(1),
                                new GrantEffectToOwnCreaturesUntilEndOfCombatEffect(
                                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                        new PutCountersOnSourceEffect(1, 1, 1),
                                        new PermanentIsAttackingPredicate()))));
    }
}
