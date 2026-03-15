package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturePerCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "100")
public class Bonehoard extends Card {

    public Bonehoard() {
        // Living weapon — create 0/0 black Phyrexian Germ token and attach
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LivingWeaponEffect());

        // Equipped creature gets +X/+X, where X is the number of creature cards in all graveyards
        addEffect(EffectSlot.STATIC, new BoostCreaturePerCardsInAllGraveyardsEffect(
                new CardTypePredicate(CardType.CREATURE), GrantScope.EQUIPPED_CREATURE));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
