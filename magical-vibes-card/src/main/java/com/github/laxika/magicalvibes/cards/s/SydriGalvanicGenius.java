package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C13", collectorNumber = "220")
public class SydriGalvanicGenius extends Card {

    public SydriGalvanicGenius() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new AnimatePermanentsEffect(
                        new TargetManaValue(), new TargetManaValue(),
                        List.of(), Set.of(), null, Set.of(),
                        GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN, null
                )),
                "{U}: Target noncreature artifact becomes an artifact creature with power and toughness each equal to its mana value until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentNotPredicate(new PermanentIsCreaturePredicate())
                        )),
                        "Target must be a noncreature artifact"
                )
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}{B}",
                List.of(new GrantKeywordEffect(
                        Set.of(Keyword.DEATHTOUCH, Keyword.LIFELINK), GrantScope.TARGET)),
                "{W}{B}: Target artifact creature gains deathtouch and lifelink until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        )),
                        "Target must be an artifact creature"
                )
        ));
    }
}
