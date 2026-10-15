package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PlayersInGame;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "VOC", collectorNumber = "23")
@CardRegistration(set = "VOC", collectorNumber = "61")
public class ArterialAlchemy extends Card {

    public ArterialAlchemy() {
        PermanentHasSubtypePredicate blood = new PermanentHasSubtypePredicate(CardSubtype.BLOOD);

        // When this enchantment enters, create a Blood token for each opponent you have.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                CreateTokenEffect.ofBloodToken(new Sum(new PlayersInGame(), new Fixed(-1))));

        // Blood tokens you control are Equipment in addition to their other types.
        addEffect(EffectSlot.STATIC,
                new GrantSubtypeEffect(CardSubtype.EQUIPMENT, GrantScope.OWN_PERMANENTS, false, blood));

        // Blood tokens you control have "Equipped creature gets +2/+0".
        addEffect(EffectSlot.STATIC,
                new GrantEffectEffect(
                        new AttachedBoostEffect(new Fixed(2), new Fixed(0), GrantScope.EQUIPPED_CREATURE),
                        GrantScope.OWN_PERMANENTS,
                        blood));

        // ...and equip {2}.
        addEffect(EffectSlot.STATIC,
                new GrantActivatedAbilityEffect(
                        new EquipActivatedAbility("{2}"), GrantScope.OWN_PERMANENTS, blood));
    }
}
