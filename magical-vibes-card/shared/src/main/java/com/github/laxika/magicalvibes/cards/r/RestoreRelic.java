package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/** Restore Relic, the prepared spell of Lorehold Archivist (SOC 49). */
public class RestoreRelic extends Card {

    public RestoreRelic() {
        addSpellEffect(ExileGraveyardCardsEffect.oneTargetFromControllerGraveyard(new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.ARTIFACT),
                new CardTypePredicate(CardType.CREATURE)))));
        addSpellEffect(CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(StepKind.EXILE),
                new CreateTokenCopyOfTargetPermanentEffect()));
    }
}
