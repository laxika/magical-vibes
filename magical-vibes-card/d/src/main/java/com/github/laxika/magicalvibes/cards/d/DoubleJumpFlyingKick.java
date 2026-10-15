package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Double Jump // Flying Kick, a split card with fuse.
 *
 * <p>The first half puts a flying counter on a creature you control and sets its base power and
 * toughness to 5/5 until end of turn. The second half has a creature you control deal damage equal
 * to its power to a creature an opponent controls.</p>
 */
@CardRegistration(set = "TMC", collectorNumber = "35")
public class DoubleJumpFlyingKick extends Card {

    public DoubleJumpFlyingKick() {
        setAllowSharedTargets(true);

        TargetFilter controlledCreature = TargetFilters.creatureYouControl();
        TargetFilter opponentCreature = TargetFilters.creatureAnOpponentControls();

        CardEffect doubleJump = SequenceEffect.of(
                new PutCounterOnTargetPermanentEffect(CounterType.FLYING),
                new SetBasePowerToughnessEffect(5, 5));
        CardEffect flyingKick = new TargetCreatureDealsPowerDamageToAnyTargetEffect(0, 1, false);
        CardEffect fusedFlyingKick = new TargetCreatureDealsPowerDamageToAnyTargetEffect(1, 2, false);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Double Jump — Put a flying counter on target creature you control. Until end of turn, it has base power and toughness 5/5",
                        doubleJump,
                        controlledCreature
                ).withManaCost("{1}{U}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Flying Kick — Target creature you control deals damage equal to its power to target creature an opponent controls",
                        List.<CardEffect>of(flyingKick),
                        List.of(controlledCreature, opponentCreature)
                ).withManaCost("{1}{R}"),
                new ChooseOneEffect.ChooseOneOption(
                        "Fuse — Double Jump and then Flying Kick",
                        List.of(doubleJump, fusedFlyingKick),
                        List.of(controlledCreature, controlledCreature, opponentCreature)
                ).withManaCost("{2}{U}{R}")
        )));
    }
}
