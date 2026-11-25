package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "207")
@CardRegistration(set = "SLD", collectorNumber = "18")
@CardRegistration(set = "SLD", collectorNumber = "929")
@CardRegistration(set = "SPG", collectorNumber = "136")
@CardRegistration(set = "C13", collectorNumber = "111")
public class GoblinSharpshooter extends Card {

    public GoblinSharpshooter() {
        addStaticEffect(DoesntUntapEffect.self());
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY), new UntapPermanentsEffect(Subjects.source()));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(DealDamageEffect.toAnyTarget(1)),
                "{T}: This creature deals 1 damage to any target."
        ));
    }
}
