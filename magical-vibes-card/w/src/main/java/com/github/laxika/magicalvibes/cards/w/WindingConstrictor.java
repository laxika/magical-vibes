package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddOnePlayerCounterEffect;
import com.github.laxika.magicalvibes.model.effect.AddOneCounterToArtifactOrCreatureEffect;

@CardRegistration(set = "AER", collectorNumber = "140")
@CardRegistration(set = "SLD", collectorNumber = "309")
@CardRegistration(set = "KLR", collectorNumber = "216")
@CardRegistration(set = "PIP", collectorNumber = "223")
@CardRegistration(set = "PIP", collectorNumber = "751")
public class WindingConstrictor extends Card {

    public WindingConstrictor() {
        addStaticEffect(new AddOneCounterToArtifactOrCreatureEffect());
        addStaticEffect(new AddOnePlayerCounterEffect());
    }
}
