package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "CSP", collectorNumber = "18")
public class SunsBounty extends Card {

    public SunsBounty() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(4));

        addEffect(EffectSlot.GRAVEYARD_ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD,
                new MayPayManaEffect("{1}{W}",
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Pay {1}{W} to return Sun's Bounty from your graveyard to your hand?",
                        new ExileSourceCardFromGraveyardEffect()));
    }
}
