package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllButBottomCardOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

public class NicolBolasTheArisen extends Card {

    public NicolBolasTheArisen() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new DrawCardEffect(2)),
                "+2: Draw two cards."));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DealDamageToTargetCreatureOrPlaneswalkerEffect(10)),
                "−3: Nicol Bolas deals 10 damage to target creature or planeswalker."));

        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .filter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.PLANESWALKER))))
                        .targetGraveyard(true)
                        .build()),
                "−4: Put target creature or planeswalker card from a graveyard onto the battlefield under your control."));

        addActivatedAbility(new ActivatedAbility(
                -12,
                List.of(new ExileAllButBottomCardOfTargetLibraryEffect()),
                "−12: Exile all but the bottom card of target player's library."));
    }
}
