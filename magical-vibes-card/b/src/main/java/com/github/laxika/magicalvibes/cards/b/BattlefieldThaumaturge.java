package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.ModifyCastCostPerTargetEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "31")
public class BattlefieldThaumaturge extends Card {

    public BattlefieldThaumaturge() {
        addStaticEffect(new ModifyCastCostPerTargetEffect(-1, CostModificationScope.SELF,
                new PermanentIsCreaturePredicate(),
                new CardAnyOfPredicate(List.of(new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)))));
        addTrigger(Triggers.youCastSpell().withStackEntryFilter(new StackEntryTargetsSourcePredicate()),
                new GrantKeywordEffect(Keyword.HEXPROOF, Subjects.source()));
    }
}
