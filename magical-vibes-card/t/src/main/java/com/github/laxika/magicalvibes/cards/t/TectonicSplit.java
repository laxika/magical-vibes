package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeFractionRoundedUpCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "144")
@CardRegistration(set = "TLE", collectorNumber = "208")
public class TectonicSplit extends Card {

    public TectonicSplit() {
        addEffect(EffectSlot.SPELL,
                new SacrificeFractionRoundedUpCost(2, new PermanentIsLandPredicate()));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(true, null,
                        List.of(new AwardAnyColorManaEffect(3)),
                        "{T}: Add three mana of any one color."),
                GrantScope.OWN_LANDS));
    }
}
