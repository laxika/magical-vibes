package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;

@CardRegistration(set = "VOW", collectorNumber = "90")
@CardRegistration(set = "DBL", collectorNumber = "357")
public class WitnessTheFuture extends Card {

    public WitnessTheFuture() {
        addSpellEffect(new ShuffleTargetCardsFromGraveyardIntoLibraryEffect(null, 4));
        addSpellEffect(LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(
                new Fixed(4), 1));
    }
}
