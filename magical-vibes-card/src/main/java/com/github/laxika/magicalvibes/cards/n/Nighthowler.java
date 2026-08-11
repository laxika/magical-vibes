package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.BestowCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "98")
public class Nighthowler extends Card {

    public Nighthowler() {
        addCastingOption(new BestowCast("{2}{B}{B}"));

        CardsInGraveyard creatureCardsInAllGraveyards =
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.ANY_PLAYER);
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(creatureCardsInAllGraveyards, creatureCardsInAllGraveyards));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                        creatureCardsInAllGraveyards,
                        creatureCardsInAllGraveyards,
                        GrantScope.ENCHANTED_CREATURE));
    }
}
