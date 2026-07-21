package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "2")
public class DecimatorOfTheProvinces extends Card {

    public DecimatorOfTheProvinces() {
        // Emerge {6}{G}{G}{G} — sacrifice a creature and pay the emerge cost reduced by that
        // creature's mana value (generic only; colored components cannot be reduced).
        addCastingOption(new AlternateHandCast(List.of(
                new ManaCastingCost("{6}{G}{G}{G}"),
                new SacrificePermanentsCost(1, new PermanentIsCreaturePredicate())
        ), true));

        // When you cast this spell, creatures you control get +2/+2 and gain trample until end of turn.
        addEffect(EffectSlot.ON_SELF_CAST, new BoostAllOwnCreaturesEffect(2, 2));
        addEffect(EffectSlot.ON_SELF_CAST, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES));
    }
}
