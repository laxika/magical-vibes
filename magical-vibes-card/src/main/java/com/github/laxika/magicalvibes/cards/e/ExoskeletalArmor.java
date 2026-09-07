package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JUD", collectorNumber = "114")
public class ExoskeletalArmor extends Card {

    public ExoskeletalArmor() {
        CardsInGraveyard creatureCardsInAllGraveyards =
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.ANY_PLAYER);

        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                        creatureCardsInAllGraveyards,
                        creatureCardsInAllGraveyards,
                        GrantScope.ENCHANTED_CREATURE));
    }
}
