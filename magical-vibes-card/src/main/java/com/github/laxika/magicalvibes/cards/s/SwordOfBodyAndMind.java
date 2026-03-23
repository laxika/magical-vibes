package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOM", collectorNumber = "208")
public class SwordOfBodyAndMind extends Card {

    public SwordOfBodyAndMind() {
        // Static: equipped creature gets +2/+2
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));

        // Static: equipped creature has protection from green and from blue
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.GREEN, CardColor.BLUE), GrantScope.EQUIPPED_CREATURE));

        // Triggered: whenever equipped creature deals combat damage to a player,
        // you create a 2/2 green Wolf creature token
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new CreateTokenEffect(1, "Wolf", 2, 2,
                        CardColor.GREEN, List.of(CardSubtype.WOLF),
                        Set.of(), Set.of()));

        // Triggered: ...and that player mills ten cards
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MillTargetPlayerEffect(10));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
