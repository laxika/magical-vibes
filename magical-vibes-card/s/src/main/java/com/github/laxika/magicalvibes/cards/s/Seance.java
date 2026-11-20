package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "20")
@CardRegistration(set = "MM3", collectorNumber = "22")
@CardRegistration(set = "SIS", collectorNumber = "12")
public class Seance extends Card {

    public Seance() {
        // At the beginning of each upkeep, you may exile target creature card from your graveyard.
        // If you do, create a token that's a copy of that card, except it's a Spirit in addition
        // to its other types. Exile it at the beginning of the next end step.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new MayEffect(
                        SequenceEffect.of(
                                ExileGraveyardCardsEffect.oneTargetFromControllerGraveyard(
                                        new CardTypePredicate(CardType.CREATURE)),
                                CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(StepKind.EXILE),
                                        new CreateTokenCopyOfTargetPermanentEffect(List.of(CardSubtype.SPIRIT),
                                                Set.of(), null, null, Map.of(), false, true, false, false, false,
                                                false, null, Set.of()))),
                        "You may exile target creature card from your graveyard."
                ));
    }
}
