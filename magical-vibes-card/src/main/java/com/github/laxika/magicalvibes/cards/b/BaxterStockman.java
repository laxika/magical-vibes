package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "139")
public class BaxterStockman extends Card {

    public BaxterStockman() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                1, "Robot", 1, 1, null,
                List.of(CardSubtype.ROBOT), Set.of(), Set.of(CardType.ARTIFACT)));

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                "Target must be an artifact creature you control"
        )).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new BoostTargetCreatureEffect(3, 0))
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.VIGILANCE), GrantScope.TARGET));
    }
}
