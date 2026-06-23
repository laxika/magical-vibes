package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.w.WithengarUnbound;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

@CardRegistration(set = "DKA", collectorNumber = "147")
public class Elbrus extends Card {

    public Elbrus() {
        // Set up back face
        WithengarUnbound backFace = new WithengarUnbound();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // Equipped creature gets +1/+0.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));

        // When equipped creature deals combat damage to a player, unattach Elbrus, then transform it.
        // (The unattach is handled in AnimationResolutionService.resolveTransformSelf: an attached
        // Equipment transforming into a non-Equipment becomes unattached.)
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new TransformSelfEffect());

        // Equip {1}
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "WithengarUnbound";
    }
}
