package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "68")
public class MasterOfWinds extends Card {

    public MasterOfWinds() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DrawCardEffect(2),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER));

        CardAnyOfPredicate instantSorceryOrWizard = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY),
                new CardSubtypePredicate(CardSubtype.WIZARD)));
        ChooseOneEffect basePowerAndToughness = new ChooseOneEffect(List.of(
                new ChooseOneOption("4/1", new SetBasePowerToughnessEffect(4, 1, GrantScope.SELF)),
                new ChooseOneOption("1/4", new SetBasePowerToughnessEffect(1, 4, GrantScope.SELF))));
        addTrigger(Triggers.youCastSpell().withFilter(instantSorceryOrWizard),
                new MayEffect(new ChooseOneAtResolutionEffect(basePowerAndToughness), "Have Master of Winds's base power and toughness become 4/1 or 1/4?"));
    }
}
