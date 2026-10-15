package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "110")
public class HordeAmbusher extends Card {

    public HordeAmbusher() {
        addMorph("{1}{R}", new CardColorPredicate(CardColor.RED), "red");
        addEffect(EffectSlot.ON_BLOCK, new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER));
        target(TargetFilters.creature()).addEffect(
                EffectSlot.ON_TURNED_FACE_UP,
                new CantBlockThisTurnEffect(TapUntapScope.TARGET));
    }
}
