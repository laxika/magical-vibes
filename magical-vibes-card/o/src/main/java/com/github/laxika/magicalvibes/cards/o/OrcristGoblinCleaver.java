package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureTypeAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasTypeChosenThisResolutionPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "177")
public class OrcristGoblinCleaver extends Card {

    public OrcristGoblinCleaver() {
        addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.attachedCreature()));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ChooseCreatureTypeAtResolutionEffect(),
                CreateTokenEffect.ofTreasureToken(new PermanentCount(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(), new PermanentHasTypeChosenThisResolutionPredicate())),
                        CountScope.CONTROLLER)));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
