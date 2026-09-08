package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealHandChooseCardFromItAndExileAllCopiesEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M13", collectorNumber = "109")
@CardRegistration(set = "FUT", collectorNumber = "76")
public class ShimianSpecter extends Card {

    public ShimianSpecter() {
        // The damaged player is baked in as the trigger's target, so no target(...) is declared.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new RevealHandChooseCardFromItAndExileAllCopiesEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
    }
}
