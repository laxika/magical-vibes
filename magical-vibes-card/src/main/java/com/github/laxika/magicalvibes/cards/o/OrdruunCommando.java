package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "137")
public class OrdruunCommando extends Card {

    public OrdruunCommando() {
        addActivatedAbility(new ActivatedAbility(false, "{W}",
                List.of(PreventDamageEffect.nextToSelf(1)),
                "{W}: Prevent the next 1 damage that would be dealt to this creature this turn."));
    }
}
