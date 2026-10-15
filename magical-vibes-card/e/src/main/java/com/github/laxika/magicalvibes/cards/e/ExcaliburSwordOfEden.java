package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentManaValueSum;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHistoricPredicate;

@CardRegistration(set = "ACR", collectorNumber = "72")
public class ExcaliburSwordOfEden extends Card {

    public ExcaliburSwordOfEden() {
        PermanentHasSupertypePredicate legendaryCreature =
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY);
        setAttachRestriction(legendaryCreature);

        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new PermanentManaValueSum(
                new PermanentIsHistoricPredicate(), CountScope.CONTROLLER)));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(10, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.EQUIPPED_CREATURE));

        addActivatedAbility(new EquipActivatedAbility(
                "{2}", legendaryCreature, "Target must be a legendary creature you control"));
    }
}
