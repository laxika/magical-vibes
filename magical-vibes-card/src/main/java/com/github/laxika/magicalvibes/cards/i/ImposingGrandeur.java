package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongOwnedCommanders;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDiscardHandThenDrawEffect;

@CardRegistration(set = "VOC", collectorNumber = "24")
@CardRegistration(set = "VOC", collectorNumber = "62")
public class ImposingGrandeur extends Card {

    public ImposingGrandeur() {
        addEffect(EffectSlot.SPELL, new EachPlayerMayDiscardHandThenDrawEffect(
                new GreatestManaValueAmongOwnedCommanders()));
    }
}
