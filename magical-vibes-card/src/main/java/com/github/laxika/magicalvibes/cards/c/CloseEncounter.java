package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.amount.ChosenCreatureOrWarpedCardPower;
import com.github.laxika.magicalvibes.model.effect.ChooseCreatureOrWarpedCardCost;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "EOE", collectorNumber = "176")
public class CloseEncounter extends Card {

    public CloseEncounter() {
        addEffect(EffectSlot.SPELL, new ChooseCreatureOrWarpedCardCost());
        SpellTarget target = target(TargetFilters.creature());
        target.addEffect(EffectSlot.SPELL,
                new DealDamageToTargetCreatureEffect(new ChosenCreatureOrWarpedCardPower()));
    }
}
