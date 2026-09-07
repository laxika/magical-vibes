package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TakeItBack;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "WOE", collectorNumber = "237")
public class SpellscornCoven extends Card {

    public SpellscornCoven() {
        setBackFaceCard(new TakeItBack());
        addCastingOption(new AdventureCast("{2}{U}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }

    @Override
    public String getBackFaceClassName() {
        return "TakeItBack";
    }
}
