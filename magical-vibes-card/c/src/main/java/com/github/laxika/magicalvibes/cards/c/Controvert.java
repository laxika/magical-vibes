package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "CSP", collectorNumber = "30")
public class Controvert extends Card {

    public Controvert() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());

        addEffect(EffectSlot.GRAVEYARD_ON_CREATURE_PUT_INTO_CONTROLLER_GRAVEYARD_FROM_BATTLEFIELD,
                new MayPayManaEffect("{2}{U}{U}",
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect(),
                        "Pay {2}{U}{U} to return Controvert from your graveyard to your hand?",
                        new ExileSourceCardFromGraveyardEffect()));
    }
}
