package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TieredManaCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "165")
public class ThunderMagic extends Card {

    public ThunderMagic() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Thunder - {0} - Thunder Magic deals 2 damage to target creature",
                        new DealDamageToTargetCreatureEffect(2), TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Thundara - {3} - Thunder Magic deals 4 damage to target creature",
                        new DealDamageToTargetCreatureEffect(4), TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption(
                        "Thundaga - {5}{R} - Thunder Magic deals 8 damage to target creature",
                        new DealDamageToTargetCreatureEffect(8), TargetFilters.creature())
        )));
        addEffect(EffectSlot.SPELL, new TieredManaCost(List.of("", "{3}", "{5}{R}")));
    }
}
