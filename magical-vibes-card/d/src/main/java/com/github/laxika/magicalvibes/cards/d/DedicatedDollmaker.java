package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YWOE", collectorNumber = "2")
public class DedicatedDollmaker extends Card {

    public DedicatedDollmaker() {
        PermanentPredicateTargetFilter otherNonlandNontokenPermanent = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate()),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                )),
                "Target must be another nonland, nontoken permanent");

        target(otherNonlandNontokenPermanent, 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        ExileTargetPermanentAndCreateTokenCopyEffect.forTargetController(
                                Set.of(CardType.ARTIFACT), true));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(new GrantKeywordEffect(
                        Keyword.INDESTRUCTIBLE,
                        GrantScope.OWN_PERMANENTS,
                        new PermanentIsTokenPredicate())),
                "{1}{W}: Tokens you control gain indestructible until end of turn. Activate only once."
        ).withMaxActivationsPerGame(1));
    }
}
