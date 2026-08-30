package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "132")
public class OrderChaos extends Card {

    public OrderChaos() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Order — Exile target attacking creature",
                        new ExileTargetPermanentEffect(),
                        TargetFilters.attackingCreature()
                ).withManaCost("{3}{W}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Chaos — Creatures can't block this turn",
                        new CantBlockThisTurnEffect(TapUntapScope.ALL_CREATURES)
                ).withManaCost("{2}{R}")
        )));
    }
}
