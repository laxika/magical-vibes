package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "151")
public class DevilsPlayground extends Card {

    public DevilsPlayground() {
        Map<EffectSlot, CardEffect> tokenEffects =
                Map.of(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                4, "Devil", 1, 1, CardColor.RED,
                List.of(CardSubtype.DEVIL), Set.of(), Set.of(), tokenEffects));
    }
}
