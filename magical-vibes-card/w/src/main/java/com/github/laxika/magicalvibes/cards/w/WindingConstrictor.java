package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "140")
@CardRegistration(set = "SLD", collectorNumber = "309")
@CardRegistration(set = "KLR", collectorNumber = "216")
@CardRegistration(set = "PIP", collectorNumber = "223")
@CardRegistration(set = "PIP", collectorNumber = "751")
public class WindingConstrictor extends Card {

    public WindingConstrictor() {
        addStaticEffect(AddOnePlusOneCountersEffect.plusOne(CounterType.ANY,
                AddOnePlusOneCountersEffect.youControl(new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate())))));
        addStaticEffect(AddOnePlusOneCountersEffect.plusOneForYou());
    }
}
