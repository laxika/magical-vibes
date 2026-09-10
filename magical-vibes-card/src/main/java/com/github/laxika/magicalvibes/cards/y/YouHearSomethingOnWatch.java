package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "42")
public class YouHearSomethingOnWatch extends Card {

    public YouHearSomethingOnWatch() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures you control get +1/+1 until end of turn",
                        new BoostAllOwnCreaturesEffect(1, 1)),
                new ChooseOneEffect.ChooseOneOption(
                        "This spell deals 5 damage to target attacking creature",
                        new DealDamageToTargetCreatureEffect(5),
                        TargetFilters.attackingCreature())
        )));
    }
}
