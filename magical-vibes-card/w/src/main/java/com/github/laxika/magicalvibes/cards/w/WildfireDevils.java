package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "C21", collectorNumber = "183")
@CardRegistration(set = "C19", collectorNumber = "30")
@CardRegistration(set = "SCD", collectorNumber = "170")
public class WildfireDevils extends Card {

    public WildfireDevils() {
        SequenceEffect ability = SequenceEffect.of(
                new ChoosePlayerAtRandomEffect(Subjects.eachPlayer()),
                ChooseCardEffect.oneOf(
                        Subjects.cardsInGraveyards(Subjects.playersThisWay(StepKind.CHOOSE),
                                new CardAnyOfPredicate(List.of(
                                        new CardTypePredicate(CardType.INSTANT),
                                        new CardTypePredicate(CardType.SORCERY)))),
                        "an instant or sorcery card from your graveyard to exile")
                        .by(Subjects.playersThisWay(StepKind.CHOOSE)),
                ExileGraveyardCardsEffect.cards(Subjects.cardsThisWay(StepKind.CHOOSE)),
                CopyImprintedCardAndMayCastCopyEffect.of(Subjects.cardsThisWay(StepKind.EXILE)));
        addTrigger(Triggers.enters(ObjectRelation.THIS), ability);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), ability);
    }
}
