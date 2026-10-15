package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

@CardRegistration(set = "FIC", collectorNumber = "100")
@CardRegistration(set = "FIC", collectorNumber = "128")
public class WreckingBallArm extends Card {

    public WreckingBallArm() {
        addEffect(EffectSlot.STATIC,
                new SetBasePowerToughnessEffect(7, 7, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentPowerAtMostPredicate(2)));

        addActivatedAbility(new EquipActivatedAbility(
                "{3}",
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                "Target must be a legendary creature you control"));
        addActivatedAbility(new EquipActivatedAbility("{7}"));
    }
}
