package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegenerateAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "180")
public class FullMoonsRise extends Card {

    public FullMoonsRise() {
        // Werewolf creatures you control get +1/+0 and have trample.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, Set.of(Keyword.TRAMPLE), GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.WEREWOLF))));

        // Sacrifice Full Moon's Rise: Regenerate all Werewolf creatures you control.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new RegenerateAllOwnCreaturesEffect(new PermanentHasSubtypePredicate(CardSubtype.WEREWOLF))
                ),
                "Sacrifice Full Moon's Rise: Regenerate all Werewolf creatures you control."
        ));
    }
}
