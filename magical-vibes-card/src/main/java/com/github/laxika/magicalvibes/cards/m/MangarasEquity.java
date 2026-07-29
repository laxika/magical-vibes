package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.ReflectDamageToChosenColorCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "26")
public class MangarasEquity extends Card {

    public MangarasEquity() {
        // As this enchantment enters, choose black or red.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseColorOnEnterEffect(CardColor.BLACK, CardColor.RED));

        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {1}{W}.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new PayManaCost("{1}{W}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        // Whenever a creature of the chosen color deals damage to you or a white creature you
        // control, this enchantment deals that much damage to that creature.
        addEffect(EffectSlot.ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT,
                new ReflectDamageToChosenColorCreatureEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.WHITE))))));
    }
}
