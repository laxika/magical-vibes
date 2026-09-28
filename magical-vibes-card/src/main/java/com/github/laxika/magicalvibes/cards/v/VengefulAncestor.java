package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsGoadedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TDC", collectorNumber = "242")
@CardRegistration(set = "MKC", collectorNumber = "163")
public class VengefulAncestor extends Card {

    public VengefulAncestor() {
        GoadTargetCreatureUntilNextTurnEffect goad = new GoadTargetCreatureUntilNextTurnEffect();
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, goad)
                .addEffect(EffectSlot.ON_ATTACK, goad);

        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsGoadedPredicate(),
                        new TargetCreatureDealsDamageToControllerEffect(1)));
    }
}
