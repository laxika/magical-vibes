package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPutIntoGraveyardFromBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "86")
public class SupperForSpiders extends Card {

    public SupperForSpiders() {
        // "They are Food artifacts" floats layer-4 and layer-6 effects on the entered permanents, not copiable values
        // (CR 707.2): a copy of a Food copies the creature card, and a Food that becomes a copy stays a Food artifact.
        addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.cardsInGraveyards(Subjects.eachOpponent(),
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardPutIntoGraveyardFromBattlefieldThisTurnPredicate()))))
                .underControlOf(Subjects.you())
                .enteringWith(new SetCardTypesEffect(Set.of(CardType.ARTIFACT), Subjects.target()),
                        new GrantSubtypeEffect(CardSubtype.FOOD, Subjects.target(), true),
                        new GrantActivatedAbilityEffect(new ActivatedAbility(true, "{2}",
                                List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                                "{2}, {T}, Sacrifice this artifact: You gain 3 life."), Subjects.target())));
    }
}
