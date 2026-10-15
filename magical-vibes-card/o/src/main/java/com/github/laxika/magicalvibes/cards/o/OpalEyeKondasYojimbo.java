package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageFromChosenSourceToSelfEffect;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "17")
public class OpalEyeKondasYojimbo extends Card {

    public OpalEyeKondasYojimbo() {
        addEffect(EffectSlot.ON_BLOCK, new BushidoEffect(1));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BushidoEffect(1));

        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new RedirectNextDamageFromChosenSourceToSelfEffect()),
                "{T}: The next time a source of your choice would deal damage this turn, "
                        + "that damage is dealt to Opal-Eye, Konda's Yojimbo instead."));
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}",
                List.of(PreventDamageEffect.nextToSelf(1)),
                "{1}{W}: Prevent the next 1 damage that would be dealt to Opal-Eye, Konda's Yojimbo this turn."));
    }
}
