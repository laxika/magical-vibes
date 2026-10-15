package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "AFC", collectorNumber = "4")
public class VrondissRageOfAncients extends Card {

    public VrondissRageOfAncients() {
        Map<EffectSlot, CardEffect> dragonSpiritTokenEffects =
                Map.of(EffectSlot.ON_SELF_DEALS_DAMAGE, new SacrificeSelfEffect());
        CreateTokenEffect dragonSpiritToken = new CreateTokenEffect(
                1, "Dragon Spirit", 5, 4, CardColor.RED, Set.of(CardColor.RED, CardColor.GREEN),
                List.of(CardSubtype.DRAGON, CardSubtype.SPIRIT))
                .withTokenEffects(dragonSpiritTokenEffects);

        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new MayEffect(dragonSpiritToken, "Create a 5/4 Dragon Spirit creature token?"));
        addEffect(EffectSlot.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE,
                new MayEffect(new DealDamageToSourceEffect(1),
                        "Have Vrondiss deal 1 damage to itself?"));
    }
}
