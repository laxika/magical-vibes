package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GTC", collectorNumber = "62")
public class DeathsApproach extends Card {

    public DeathsApproach() {
        CardsInGraveyard creatureCardsInAttachedControllersGraveyard =
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.ATTACHED_CONTROLLER);
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                new Scaled(creatureCardsInAttachedControllersGraveyard, -1),
                new Scaled(creatureCardsInAttachedControllersGraveyard, -1),
                GrantScope.ENCHANTED_CREATURE));
    }
}
