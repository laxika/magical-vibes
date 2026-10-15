package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "89")
public class ZombieTrailblazer extends Card {

    public ZombieTrailblazer() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapCreatureCost(new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)),
                        new GrantBasicLandTypeToTargetEffect(
                                EffectDuration.UNTIL_END_OF_TURN, CardSubtype.SWAMP, true)),
                "Tap an untapped Zombie you control: Target land becomes a Swamp until end of turn.",
                TargetFilters.land()
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapCreatureCost(new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE)),
                        new GrantKeywordEffect(Keyword.SWAMPWALK, GrantScope.TARGET)),
                "Tap an untapped Zombie you control: Target creature gains swampwalk until end of turn.",
                TargetFilters.creature()
        ));
    }
}
