package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "MOM", collectorNumber = "56")
public class ExpeditionLookout extends Card {

    public ExpeditionLookout() {
        OpponentGraveyardAtLeast threshold = new OpponentGraveyardAtLeast(8);
        addEffect(EffectSlot.STATIC, new ConditionalEffect(threshold,
                new CanAttackAsThoughNoDefenderEffect()));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(threshold,
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));
    }
}
