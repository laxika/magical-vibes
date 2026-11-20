package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "165")
public class TerritorialBruntar extends Card {

    public TerritorialBruntar() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), SequenceEffect.of(
                ExileCardsFromTopUntilEffect.untilNonland(Subjects.you()),
                AllowPlayCardsFromExileEffect.thisTurn(
                        Subjects.cardsThisWay(StepKind.EXILE, ExileCardsFromTopUntilEffect.NONLAND))));
    }
}
