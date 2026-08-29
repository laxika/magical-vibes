package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "159")
public class NecroticSliver extends Card {

    public NecroticSliver() {
        ActivatedAbility destroyAbility = new ActivatedAbility(
                false,
                "{3}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{3}, Sacrifice this permanent: Destroy target permanent.",
                TargetFilters.permanent()
        );
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                destroyAbility,
                GrantScope.ALL_CREATURES,
                sliver
        ));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                destroyAbility,
                GrantScope.SELF,
                sliver
        ));
    }
}
