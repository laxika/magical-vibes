package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "INV", collectorNumber = "105")
public class ExoticCurse extends Card {

    public ExoticCurse() {
        target(TargetFilters.creature());

        BasicLandTypesAmongControlledLands domain = new BasicLandTypesAmongControlledLands();
        Scaled penalty = new Scaled(domain, -1);
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(penalty, penalty, GrantScope.ENCHANTED_CREATURE));
    }
}
