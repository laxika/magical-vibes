package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "TDM", collectorNumber = "13")
public class FuriousForebear extends Card {

    public FuriousForebear() {
        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_DIES,
                new MayPayManaEffect("{1}{W}",
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Pay {1}{W} to return Furious Forebear from your graveyard to your hand?"));
    }
}
