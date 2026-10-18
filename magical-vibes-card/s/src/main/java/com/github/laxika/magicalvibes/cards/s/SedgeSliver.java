package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControllerControlsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "177")
@CardRegistration(set = "SLD", collectorNumber = "642")
@CardRegistration(set = "TSR", collectorNumber = "187")
public class SedgeSliver extends Card {

    public SedgeSliver() {
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);
        PermanentControllerControlsPermanentPredicate controllerControlsSwamp =
                new PermanentControllerControlsPermanentPredicate(
                        new PermanentHasSubtypePredicate(CardSubtype.SWAMP));

        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new StaticBoostEffect(1, 1, GrantScope.SELF, controllerControlsSwamp),
                GrantScope.ALL_CREATURES_INCLUDING_SELF,
                sliver));

        ActivatedAbility regenerateAbility = new ActivatedAbility(
                false,
                "{B}",
                List.of(new RegenerateEffect()),
                "{B}: Regenerate this permanent."
        );
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                regenerateAbility,
                GrantScope.ALL_PERMANENTS,
                sliver
        ));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                regenerateAbility,
                GrantScope.SELF,
                sliver
        ));
    }
}
