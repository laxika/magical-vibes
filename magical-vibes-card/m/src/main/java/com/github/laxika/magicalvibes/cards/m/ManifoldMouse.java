package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "143")
public class ManifoldMouse extends Card {

    public ManifoldMouse() {
        addStaticEffect(new KickerEffect("{2}"));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new Kicked()),
                new CreateTokenCopyOfSourceEffect(false, 1, null, null, false, 1, 1));

        target(new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.MOUSE),
                        new PermanentControlledBySourceControllerPredicate())),
                "Target must be a Mouse you control"))
                .addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                        new ChooseOneForTargetCreatureEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption("Double strike",
                                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET)),
                                new ChooseOneEffect.ChooseOneOption("Trample",
                                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)))));
    }
}
