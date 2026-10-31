package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;

import java.util.Set;

@CardRegistration(set = "OGW", collectorNumber = "63")
public class SphinxOfTheFinalWord extends Card {

    public SphinxOfTheFinalWord() {
        addStaticEffect(new CantBeCounteredEffect());
        addStaticEffect(new ControllerSpellsCantBeCounteredEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY)));
    }
}
