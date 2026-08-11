package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventionScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "4")
public class AtalyaSamiteMaster extends Card {

    public AtalyaSamiteMaster() {
        // {X}, {T}: Prevent the next X damage that would be dealt to target creature this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new PreventDamageEffect(
                        PreventionScope.NEXT_TO_TARGET_CREATURE, new XValue(), false, null, null, null)),
                "{X}, {T}: Prevent the next X damage that would be dealt to target creature this turn. Spend only white mana on X.",
                TargetFilters.creature()).withXColorRestriction(ManaColor.WHITE));

        // {X}, {T}: You gain X life.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}",
                List.of(new GainLifeEffect(new XValue())),
                "{X}, {T}: You gain X life. Spend only white mana on X."
        ).withXColorRestriction(ManaColor.WHITE));
    }
}
