package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "6")
public class ArchangelElspeth extends Card {

    public ArchangelElspeth() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokenEffect(
                        1,
                        "Soldier",
                        1,
                        1,
                        CardColor.WHITE,
                        List.of(CardSubtype.SOLDIER),
                        Set.of(Keyword.LIFELINK),
                        Set.of()
                )),
                "+1: Create a 1/1 white Soldier creature token with lifelink."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        new GrantSubtypeToTargetCreatureEffect(CardSubtype.ANGEL),
                        new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET,
                                GrantDuration.INDEFINITE)
                ),
                "−2: Put two +1/+1 counters on target creature. It becomes an Angel in addition to its other types and gains flying.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                                new CardMaxManaValuePredicate(3)
                        )))
                        .returnAll(true)
                        .build()),
                "−6: Return all nonland permanent cards with mana value 3 or less from your graveyard to the battlefield."
        ));
    }
}
