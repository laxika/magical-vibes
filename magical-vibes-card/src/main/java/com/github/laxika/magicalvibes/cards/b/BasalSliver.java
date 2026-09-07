package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "96")
public class BasalSliver extends Card {

    public BasalSliver() {
        ActivatedAbility manaAbility = new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.BLACK, 2)),
                "Sacrifice this permanent: Add {B}{B}."
        );
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                manaAbility,
                GrantScope.ALL_CREATURES,
                sliver
        ));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                manaAbility,
                GrantScope.SELF,
                sliver
        ));
    }
}
