package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAndAttachToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;
import java.util.Map;

@CardRegistration(set = "MSH", collectorNumber = "236")
public class USAgentJohnWalker extends Card {

    public USAgentJohnWalker() {
        CreateTokenEffect sturdyShield = CreateTokenEffect.ofArtifactToken(
                1, "Sturdy Shield", List.of(CardSubtype.EQUIPMENT), List.of(new EquipActivatedAbility("{2}")))
                .withTokenEffects(Map.of(
                        EffectSlot.STATIC, new StaticBoostEffect(1, 2, GrantScope.EQUIPPED_CREATURE)));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenAndAttachToSourceEffect(sturdyShield));
    }
}
