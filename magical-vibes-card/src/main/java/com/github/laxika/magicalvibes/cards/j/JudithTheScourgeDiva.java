package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;

@CardRegistration(set = "RNA", collectorNumber = "185")
public class JudithTheScourgeDiva extends Card {

    private static final DealDamageToAnyTargetEffect DEATH_DAMAGE = new DealDamageToAnyTargetEffect(1);
    private static final TriggeringCardConditionalEffect NON_TOKEN_DEATH_DAMAGE =
            new TriggeringCardConditionalEffect(new CardNotPredicate(new CardIsTokenPredicate()), DEATH_DAMAGE);

    public JudithTheScourgeDiva() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, NON_TOKEN_DEATH_DAMAGE);
        addEffect(EffectSlot.ON_DEATH, NON_TOKEN_DEATH_DAMAGE);
    }
}
