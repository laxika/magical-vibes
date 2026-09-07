package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "61")
public class BalthorTheDefiled extends Card {

    public BalthorTheDefiled() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ALL_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.MINION)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{B}{B}",
                List.of(
                        new ExileSelfCost(),
                        new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                                Integer.MAX_VALUE,
                                new CardAllOfPredicate(List.of(
                                        new CardTypePredicate(CardType.CREATURE),
                                        new CardAnyOfPredicate(List.of(
                                                new CardColorPredicate(CardColor.BLACK),
                                                new CardColorPredicate(CardColor.RED))))))),
                "{B}{B}{B}, Exile Balthor the Defiled: Each player returns all black and all red creature cards from their graveyard to the battlefield."));
    }
}
