package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;

import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "311")
@CardRegistration(set = "MB2", collectorNumber = "547")
public class LifeningElemental extends Card {

    public LifeningElemental() {
        addStaticEffect(SpliceEffect.ontoInstantOrSorcery("{1}{B}", Set.of(Keyword.LIFELINK)));
    }
}
