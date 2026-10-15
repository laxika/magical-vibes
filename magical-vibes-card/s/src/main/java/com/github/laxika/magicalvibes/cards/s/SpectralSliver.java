package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "83")
@CardRegistration(set = "H09", collectorNumber = "17")
public class SpectralSliver extends Card {

    public SpectralSliver() {
        ActivatedAbility boostAbility = new ActivatedAbility(
                false,
                "{2}",
                List.of(new BoostSelfEffect(1, 1)),
                "{2}: This creature gets +1/+1 until end of turn."
        );
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                boostAbility,
                GrantScope.ALL_CREATURES_INCLUDING_SELF,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)
        ));
    }
}
