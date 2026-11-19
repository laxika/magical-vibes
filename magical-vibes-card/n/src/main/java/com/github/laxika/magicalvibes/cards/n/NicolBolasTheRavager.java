package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "M19", collectorNumber = "218")
@CardRegistration(set = "SLD", collectorNumber = "1158")
@CardRegistration(set = "SLD", collectorNumber = "1211")
public class NicolBolasTheRavager extends Card {

    public NicolBolasTheRavager() {
        setBackFaceCard(new NicolBolasTheArisen());

        addTrigger(Triggers.enters(ObjectRelation.THIS), new DiscardEffect(1, Subjects.eachOpponent()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U}{B}{R}",
                List.of(new ExileSelfAndReturnTransformedEffect()),
                "{4}{U}{B}{R}: Exile Nicol Bolas, then return him to the battlefield transformed under his owner's control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    @Override
    public String getBackFaceClassName() {
        return "NicolBolasTheArisen";
    }
}
