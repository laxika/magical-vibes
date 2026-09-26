package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RollD4Effect;

@CardRegistration(set = "SLD", collectorNumber = "1344")
public class ArdenAngel extends Card {

    public ArdenAngel() {
        addEffect(EffectSlot.GRAVEYARD_UPKEEP_TRIGGERED,
                new RollD4Effect(new ReturnSourceCardFromGraveyardToBattlefieldEffect(false)));
    }
}
