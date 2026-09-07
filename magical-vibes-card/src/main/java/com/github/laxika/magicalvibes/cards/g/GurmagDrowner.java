package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExploitEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;

@CardRegistration(set = "DTK", collectorNumber = "57")
public class GurmagDrowner extends Card {

    public GurmagDrowner() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ExploitEffect(), "Sacrifice a creature?"));
        addEffect(EffectSlot.ON_EXPLOIT,
                new LookAtTopCardsEffect(new Fixed(4), new Fixed(1), null,
                        LookDestination.GRAVEYARD, false));
    }
}
