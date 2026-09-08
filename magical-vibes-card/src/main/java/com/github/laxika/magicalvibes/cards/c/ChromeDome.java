package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "172")
@CardRegistration(set = "TMT", collectorNumber = "276")
public class ChromeDome extends Card {

    public ChromeDome() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new CreateTokenCopyOfTargetPermanentEffect(true, false, true)),
                "{5}, {T}: Create a token that's a copy of another target artifact you control. That token gains haste. Sacrifice it at the beginning of the next end step.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        "Target must be another artifact you control.")));
    }
}
