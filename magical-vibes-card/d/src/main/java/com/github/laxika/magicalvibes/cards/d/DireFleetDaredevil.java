package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "99")
@CardRegistration(set = "AFC", collectorNumber = "121")
@CardRegistration(set = "OTC", collectorNumber = "162")
@CardRegistration(set = "LCC", collectorNumber = "223")
public class DireFleetDaredevil extends Card {

    public DireFleetDaredevil() {
        // When this creature enters, exile target instant or sorcery card from an opponent's graveyard. You may
        // cast it this turn, and mana of any type can be spent to cast that spell. If that spell would be put into
        // a graveyard, exile it instead.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ExileGraveyardCardsEffect.oneTargetFromOpponentGraveyard(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY)))),
                AllowPlayCardsFromExileEffect.thisTurn(Subjects.cardsThisWay(StepKind.EXILE))
                        .withAnyManaType().exilingInsteadOfGraveyard());
    }
}
