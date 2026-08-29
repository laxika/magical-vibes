package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ROE", collectorNumber = "221")
public class PennonBlade extends Card {

    public PennonBlade() {
        PermanentCount creaturesYouControl = new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                creaturesYouControl, creaturesYouControl, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
