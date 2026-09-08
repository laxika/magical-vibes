package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "253")
public class CrossroadsCandleguide extends Card {

    public CrossroadsCandleguide() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ExileCardsFromGraveyardEffect(1, 0));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new AwardAnyColorManaEffect()),
                "{2}: Add one mana of any color."
        ));
    }
}
