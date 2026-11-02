package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.ReflectDamageToChosenColorCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "26")
public class MangarasEquity extends Card {

    public MangarasEquity() {
        // As this enchantment enters, choose black or red.
        addStaticEffect(new ChooseColorOnEnterEffect(CardColor.BLACK, CardColor.RED));

        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {1}{W}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new ForcedCostOrElseEffect(
                        new PayManaCost("{1}{W}"),
                        List.of(new SacrificeSelfEffect()),
                        true));

        // Whenever a creature of the chosen color deals damage to you or a white creature you
        // control, this enchantment deals that much damage to that creature.
        addTrigger(Triggers.creatureDealsDamageToYouOrYourPermanent(),
                new ReflectDamageToChosenColorCreatureEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.WHITE))))));
    }
}
