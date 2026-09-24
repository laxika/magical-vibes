package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "623")
public class ScuttlingSliver extends Card {

    public ScuttlingSliver() {
        ActivatedAbility untapAbility = new ActivatedAbility(
                false,
                "{2}",
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF)),
                "{2}: Untap this creature."
        );

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                untapAbility,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)
        ));
    }
}
