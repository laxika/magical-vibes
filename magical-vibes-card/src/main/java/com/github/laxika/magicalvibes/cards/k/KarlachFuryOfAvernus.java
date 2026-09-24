package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.FirstCombatPhase;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "SLD", collectorNumber = "1802")
public class KarlachFuryOfAvernus extends Card {

    public KarlachFuryOfAvernus() {
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(new FirstCombatPhase(),
                        SequenceEffect.of(
                                new UntapPermanentsEffect(TapUntapScope.ALL_CREATURES,
                                        new PermanentIsAttackingPredicate()),
                                new GrantKeywordEffect(Keyword.FIRST_STRIKE,
                                        GrantScope.ALL_CREATURES_INCLUDING_SELF,
                                        new PermanentIsAttackingPredicate()),
                                new AdditionalCombatPhaseEffect(1))));
    }
}
