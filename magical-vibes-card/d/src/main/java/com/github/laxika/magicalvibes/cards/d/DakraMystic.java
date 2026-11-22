package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "JOU", collectorNumber = "35")
public class DakraMystic extends Card {

    public DakraMystic() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(SequenceEffect.of(
                        new RevealTopCardOfLibraryEffect(Subjects.eachPlayer()),
                        new MayEffect(
                                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.cardsThisWay(StepKind.REVEAL)),
                                "Put the revealed cards into their owners' graveyards? If you don't, each player draws a card.",
                                new EachPlayerDrawsCardEffect(1)))),
                "{U}, {T}: Each player reveals the top card of their library. You may put the revealed cards into their owners' graveyards. If you don't, each player draws a card."
        ));
    }
}
