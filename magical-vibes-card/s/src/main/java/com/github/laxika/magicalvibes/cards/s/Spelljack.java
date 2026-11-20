package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounteredSpellDestination;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "JUD", collectorNumber = "51")
public class Spelljack extends Card {

    public Spelljack() {
        addSpellEffect(new CounterSpellEffect(CounteredSpellDestination.EXILE));
        addSpellEffect(AllowPlayCardsFromExileEffect.whileExiled(Subjects.cardsThisWay(StepKind.COUNTER_SPELL))
                .withoutPayingManaCost());
    }
}
