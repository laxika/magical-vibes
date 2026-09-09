package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuperStrength.class, GiantGrowth.class, GrizzlyBears.class, IcyManipulator.class, Shock.class,
        Spellbook.class})
class SuperStrengthTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +4/+4, trample, and ward {1}")
    void grantsBoostTrampleAndWard() {
        Permanent bears = addEnchantedBears();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay {1}")
    void wardCountersUnpaidSpell() {
        Permanent bears = addEnchantedBears();
        castSpellAt(player2, new Shock(), bears);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("Paying {1} lets an opponent's spell resolve")
    void payingWardLetsSpellResolve() {
        Permanent bears = addEnchantedBears();
        castSpellAt(player2, new GiantGrowth(), bears);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(9);
    }

    @Test
    @DisplayName("Ward also counters an opponent's activated ability when they do not pay {1}")
    void wardCountersUnpaidActivatedAbility() {
        Permanent bears = addEnchantedBears();
        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        icyManipulator.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icyManipulator), null, bears.getId());

        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Super Strength cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent spellbook = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player2.getId()).add(spellbook);

        harness.setHand(player1, List.of(new SuperStrength()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, spellbook.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addEnchantedBears() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new SuperStrength());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return bears;
    }

    private void castSpellAt(Player caster, Card spell, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(spell));
        harness.addMana(caster, spell instanceof Shock ? ManaColor.RED : ManaColor.GREEN,
                spell instanceof Shock ? 1 : 2);
        harness.castInstant(caster, 0, target.getId());
    }
}
