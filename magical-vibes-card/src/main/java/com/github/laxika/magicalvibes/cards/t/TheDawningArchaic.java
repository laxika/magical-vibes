package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "1")
public class TheDawningArchaic extends Card {

    public TheDawningArchaic() {
        // This spell costs {1} less to cast for each instant and sorcery card in your graveyard.
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new CardsInGraveyard(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        CountScope.CONTROLLER)));

        // Whenever The Dawning Archaic attacks, you may cast target instant or sorcery card from your
        // graveyard without paying its mana cost. If that spell would be put into your graveyard,
        // exile it instead.
        addEffect(EffectSlot.ON_ATTACK, new CastTargetInstantOrSorceryFromGraveyardEffect(
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD, true, true));
    }
}
