package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "36")
@CardRegistration(set = "MIC", collectorNumber = "102")
public class HourOfEternity extends Card {

    public HourOfEternity() {
        // Exile X target creature cards from your graveyard. For each card exiled this way, create a
        // token that's a copy of that card, except it's a 4/4 black Zombie.
        addSpellEffect(ExileCardsFromGraveyardEffect.exactlyXFromOwnGraveyard(new CardTypePredicate(CardType.CREATURE)));
        addSpellEffect(CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(StepKind.EXILE),
                        new CreateTokenCopyOfTargetPermanentEffect(List.of(), Set.of(), 4, 4, Map.of(),
                                false, false, false, false, false, false, CardColor.BLACK, Set.of()))
                .withCreatureTypesInstead(List.of(CardSubtype.ZOMBIE)));
    }
}
