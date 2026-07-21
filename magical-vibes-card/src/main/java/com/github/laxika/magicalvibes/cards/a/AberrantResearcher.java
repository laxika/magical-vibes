package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PerfectedForm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillAndTransformIfTypesEffect;

import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "52")
public class AberrantResearcher extends Card {

    public AberrantResearcher() {
        PerfectedForm backFace = new PerfectedForm();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // At the beginning of your upkeep, mill a card. If an instant or sorcery card was
        // milled this way, transform this creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MillAndTransformIfTypesEffect(Set.of(CardType.INSTANT, CardType.SORCERY)));
    }

    @Override
    public String getBackFaceClassName() {
        return "PerfectedForm";
    }
}
