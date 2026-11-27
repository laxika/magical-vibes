package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "90")
@CardRegistration(set = "BLC", collectorNumber = "254")
public class KwainItinerantMeddler extends Card {

    public KwainItinerantMeddler() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(EachPlayerMayEffect.of(Subjects.eachPlayer(), "Draw a card?", players -> SequenceEffect.of(
                        new DrawCardEffect(1, players),
                        new GainLifeEffect(1, Subjects.playersThisWay(StepKind.DRAW))))),
                "{T}: Each player may draw a card, then each player who drew a card this way gains 1 life."));
    }
}
