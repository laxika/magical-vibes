package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.s.Snakeform;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuardiansOfAkrasa.class, CylianElf.class})
class GuardiansOfAkrasaTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new GuardiansOfAkrasa());
        Permanent bears = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(1)); // Cylian Elf attacks alone
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new GuardiansOfAkrasa());
        Permanent bears = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new GuardiansOfAkrasa());
        Permanent bears1 = addCreatureReady(player1, new CylianElf());
        addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(1, 2)); // two attack — not alone

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Guardians of Akrasa"));
        assertThat(gqs.getEffectivePower(gd, bears1)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Defender prevents Guardians of Akrasa from being declared as an attacker")
    void defenderCannotAttack() {
        Permanent guardians = addCreatureReady(player1, new GuardiansOfAkrasa());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(guardians.isAttacking()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Guardians of Akrasa contributes a separate exalted boost")
    void multipleExaltedAbilitiesStack() {
        addCreatureReady(player1, new GuardiansOfAkrasa());
        addCreatureReady(player1, new GuardiansOfAkrasa());
        Permanent elf = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(2));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exalted does not boost an opponent's lone attacker")
    void opponentAttackingAloneIsNotBoosted() {
        addCreatureReady(player1, new GuardiansOfAkrasa());
        Permanent elf = addCreatureReady(player2, new CylianElf());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    @DisplayName("An exalted trigger resolves after its source leaves the battlefield")
    void exaltedSurvivesSourceLeaving() {
        Permanent guardians = addCreatureReady(player1, new GuardiansOfAkrasa());
        Permanent elf = addCreatureReady(player1, new CylianElf());

        declareAttackers(player1, List.of(1));
        gd.playerBattlefields.get(player1.getId()).remove(guardians);
        gd.playerGraveyards.get(player1.getId()).add(guardians.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }

    @Test
    @CardUsed(Snakeform.class)
    @DisplayName("Guardians of Akrasa cannot trigger exalted after losing all abilities")
    void losingAbilitiesBeforeAttackPreventsExalted() {
        Permanent guardians = addCreatureReady(player1, new GuardiansOfAkrasa());
        Permanent elf = addCreatureReady(player1, new CylianElf());
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.setHand(player1, List.of(new Snakeform()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, guardians.getId());

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }
}
