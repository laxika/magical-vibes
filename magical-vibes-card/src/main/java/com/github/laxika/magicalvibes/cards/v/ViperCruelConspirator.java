package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingAlonePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "673")
public class ViperCruelConspirator extends Card {

    public ViperCruelConspirator() {
        PermanentAllOfPredicate attackingAloneCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsAttackingAlonePredicate()));
        PermanentPredicateTargetFilter targetFilter = new PermanentPredicateTargetFilter(
                attackingAloneCreature,
                "Target must be a creature that's attacking alone");

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new BoostTargetCreatureEffect(1, 1, attackingAloneCreature)),
                "{B}: Target creature that's attacking alone gets +1/+1 until end of turn.",
                targetFilter));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new ChooseOneForTargetCreatureEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("It gains deathtouch",
                                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET)),
                        new ChooseOneEffect.ChooseOneOption("It gains lifelink",
                                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.TARGET))))),
                "{B}: Target creature that's attacking alone gains your choice of deathtouch or lifelink until end of turn.",
                targetFilter));
    }
}
