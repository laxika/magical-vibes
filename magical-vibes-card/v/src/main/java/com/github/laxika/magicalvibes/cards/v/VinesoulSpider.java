package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SeekFromLibraryToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "YDMU", collectorNumber = "18")
public class VinesoulSpider extends Card {

    public VinesoulSpider() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new SeekFromLibraryToGraveyardEffect(new CardTypePredicate(CardType.LAND)));
    }
}
