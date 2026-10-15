package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "WWK", collectorNumber = "124")
public class HammerOfRuin extends Card {

    public HammerOfRuin() {
        // Equipped creature gets +2/+0.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature deals combat damage to a player, you may destroy target
        // Equipment that player controls.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(
                        new DestroyPermanentDamagedPlayerControlsEffect(
                                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT), 0),
                        "You may destroy target Equipment that player controls."));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
