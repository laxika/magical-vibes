package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "63")
public class CryptSliver extends Card {

    public CryptSliver() {
        ActivatedAbility regenerateAbility = new ActivatedAbility(
                true,
                null,
                List.of(new RegenerateEffect(true)),
                "{T}: Regenerate target Sliver.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.SLIVER),
                        "Target must be a Sliver"
                )
        );
        PermanentHasSubtypePredicate sliver = new PermanentHasSubtypePredicate(CardSubtype.SLIVER);

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                regenerateAbility,
                GrantScope.ALL_CREATURES,
                sliver
        ));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                regenerateAbility,
                GrantScope.SELF,
                sliver
        ));
    }
}
