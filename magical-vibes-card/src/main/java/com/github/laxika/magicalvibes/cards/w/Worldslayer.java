package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "M12", collectorNumber = "222")
@CardRegistration(set = "MRD", collectorNumber = "276")
public class Worldslayer extends Card {

    public Worldslayer() {
        // Triggered: whenever equipped creature deals combat damage to a player,
        // destroy all permanents other than this Equipment
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DestroyAllPermanentsEffect(new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

        // Equip {5}
        addActivatedAbility(new EquipActivatedAbility("{5}"));
    }
}
