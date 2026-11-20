package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPutIntoGraveyardThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "54")
@CardRegistration(set = "FDN", collectorNumber = "316")
@CardRegistration(set = "FDN", collectorNumber = "381")
@CardRegistration(set = "FDN", collectorNumber = "458")
public class AbyssalHarvester extends Card {

    public AbyssalHarvester() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardPutIntoGraveyardThisTurnPredicate())), false),
                        CreateTokenCopyOfCardEffect.of(Subjects.cardsThisWay(StepKind.EXILE),
                                new CreateTokenCopyOfTargetPermanentEffect(List.of(CardSubtype.NIGHTMARE), Set.of(),
                                        null, null, Map.of())),
                        new ExileTargetPermanentEffect(Subjects.except(
                                Subjects.controlledBy(Subjects.you(), new PermanentAllOfPredicate(List.of(
                                        new PermanentIsTokenPredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.NIGHTMARE)))),
                                Subjects.createdThisResolution()))),
                "{T}: Exile target creature card from a graveyard that was put there this turn. "
                        + "Create a token that's a copy of it, except it's a Nightmare in addition to its other types. "
                        + "Then exile all other Nightmare tokens you control."));
    }
}
