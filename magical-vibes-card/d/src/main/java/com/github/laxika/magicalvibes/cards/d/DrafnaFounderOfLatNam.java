package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryControlledByPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "47")
public class DrafnaFounderOfLatNam extends Card {

    public DrafnaFounderOfLatNam() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(ReturnToHandEffect.target()),
                "{1}{U}: Return target artifact you control to its owner's hand.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(),
                        "Target must be an artifact you control.")));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(CopySpellEffect.asToken()),
                "{3}, {T}: Copy target artifact spell you control. (The copy becomes a token.)",
                new StackEntryPredicateTargetFilter(
                        new StackEntryAllOfPredicate(List.of(
                                new StackEntryCardTypeInPredicate(Set.of(CardType.ARTIFACT)),
                                new StackEntryNotPredicate(new StackEntryTypeInPredicate(Set.of(
                                        StackEntryType.TRIGGERED_ABILITY,
                                        StackEntryType.ACTIVATED_ABILITY))),
                                new StackEntryControlledByPredicate()
                        )),
                        "Target must be an artifact spell you control.")));
    }
}
