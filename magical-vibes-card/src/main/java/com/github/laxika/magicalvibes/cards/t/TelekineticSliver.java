package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "84")
public class TelekineticSliver extends Card {

    public TelekineticSliver() {
        ActivatedAbility tapAbility = new ActivatedAbility(
                true,
                null,
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "{T}: Tap target permanent.",
                TargetFilters.permanent()
        );
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                tapAbility,
                GrantScope.ALL_CREATURES,
                sliver
        ));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                tapAbility,
                GrantScope.SELF,
                sliver
        ));
    }
}
