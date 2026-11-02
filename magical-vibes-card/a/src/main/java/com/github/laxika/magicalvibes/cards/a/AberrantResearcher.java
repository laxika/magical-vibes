package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PerfectedForm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MillAndTransformIfTypesEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "INR", collectorNumber = "52")
@CardRegistration(set = "INR", collectorNumber = "454")
@CardRegistration(set = "SOI", collectorNumber = "49")
public class AberrantResearcher extends Card {

    public AberrantResearcher() {
        setBackFaceCard(new PerfectedForm());

        // At the beginning of your upkeep, mill a card. If an instant or sorcery card was
        // milled this way, transform this creature.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MillAndTransformIfTypesEffect(Set.of(CardType.INSTANT, CardType.SORCERY)));
    }

    @Override
    public String getBackFaceClassName() {
        return "PerfectedForm";
    }
}
