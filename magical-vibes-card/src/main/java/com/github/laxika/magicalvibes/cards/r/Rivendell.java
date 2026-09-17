package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "51")
@CardRegistration(set = "HOC", collectorNumber = "91")
public class Rivendell extends Card {

    public Rivendell() {
        PermanentPredicate legendaryCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));

        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new NotCondition(new ControlsPermanent(legendaryCreature)),
                new EntersTappedEffect()));

        // {T}: Add {U}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));

        // {1}{U}, {T}: Scry 2. Activate only if you control a legendary creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{U}",
                List.of(new ScryEffect(2)),
                "{1}{U}, {T}: Scry 2. Activate only if you control a legendary creature."
        ).withActivationCondition(
                new ControlsPermanent(legendaryCreature),
                "Activate only if you control a legendary creature"));
    }
}
