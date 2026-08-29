package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "EMN", collectorNumber = "135")
public class MakeMischief extends Card {

    public MakeMischief() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(1));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1, "Devil", 1, 1, CardColor.RED,
                List.of(CardSubtype.DEVIL), Set.of(), Set.of(),
                Map.of(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1))
        ));
    }
}
