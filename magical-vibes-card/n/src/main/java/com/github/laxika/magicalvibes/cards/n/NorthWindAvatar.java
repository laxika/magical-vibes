package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.WasCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameToHandEffect;

@CardRegistration(set = "TMT", collectorNumber = "162")
@CardRegistration(set = "TMT", collectorNumber = "248")
public class NorthWindAvatar extends Card {

    public NorthWindAvatar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new WasCast(), new SearchOutsideGameToHandEffect()));
    }
}
