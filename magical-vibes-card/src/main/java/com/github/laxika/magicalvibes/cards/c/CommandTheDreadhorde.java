package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.TargetCardsManaValueSum;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "82")
public class CommandTheDreadhorde extends Card {

    public CommandTheDreadhorde() {
        CardPredicate creatureOrPlaneswalker = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.PLANESWALKER)));

        target(new GraveyardCardPredicateTargetFilter(creatureOrPlaneswalker,
                GraveyardSearchScope.ALL_GRAVEYARDS), 0, 99)
                .addEffect(EffectSlot.SPELL,
                        new DealDamageToPlayersEffect(new TargetCardsManaValueSum(), DamageRecipient.CONTROLLER))
                .addEffect(EffectSlot.SPELL,
                        ReturnTargetCardsFromGraveyardToBattlefieldEffect.fromAllGraveyards(creatureOrPlaneswalker));
    }
}
