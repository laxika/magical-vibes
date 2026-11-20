package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "127")
public class TersaLightshatter extends Card {

    public TersaLightshatter() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DiscardUpToThenDrawThatManyEffect(2));

        addTrigger(Triggers.attacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new GraveyardCardThreshold(7, new CardTruePredicate())),
                SequenceEffect.of(
                        ExileCardAtRandomEffect.fromGraveyard(Subjects.you(), null),
                        AllowPlayCardsFromExileEffect.thisTurn(Subjects.cardsThisWay(StepKind.EXILE))));
    }
}
