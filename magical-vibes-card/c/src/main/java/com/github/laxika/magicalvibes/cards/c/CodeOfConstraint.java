package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerMainPhase;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RNA", collectorNumber = "35")
public class CodeOfConstraint extends Card {

    public CodeOfConstraint() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(-4, 0))
                .addSpellEffect(new DrawCardEffect(1))
                .addSpellEffect(new ConditionalEffect(new AllOf(List.of(new WasCast(), new ControllerMainPhase())),
                        new TapPermanentsEffect(Subjects.target())))
                .addSpellEffect(new ConditionalEffect(new AllOf(List.of(new WasCast(), new ControllerMainPhase())),
                        new SkipNextUntapEffect(Subjects.target())));
    }
}
