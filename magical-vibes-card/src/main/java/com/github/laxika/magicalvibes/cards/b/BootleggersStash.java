package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "134")
public class BootleggersStash extends Card {

    public BootleggersStash() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(true, null,
                        List.of(CreateTokenEffect.ofTreasureToken(1)),
                        "{T}: Create a Treasure token."),
                GrantScope.OWN_LANDS));
    }
}
