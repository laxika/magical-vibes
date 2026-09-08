package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;

@CardRegistration(set = "VOW", collectorNumber = "90")
public class WitnessTheFuture extends Card {

    public WitnessTheFuture() {
        addEffect(EffectSlot.SPELL, new ShuffleTargetCardsFromGraveyardIntoLibraryEffect(null, 4));
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(
                new Fixed(4), 1));
    }
}
