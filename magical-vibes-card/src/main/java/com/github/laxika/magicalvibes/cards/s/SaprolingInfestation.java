package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KickedSpellCastTriggerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "208")
public class SaprolingInfestation extends Card {

    public SaprolingInfestation() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new KickedSpellCastTriggerEffect(
                List.of(new CreateTokenEffect("Saproling", 1, 1,
                        CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of()))
        ));
    }
}
