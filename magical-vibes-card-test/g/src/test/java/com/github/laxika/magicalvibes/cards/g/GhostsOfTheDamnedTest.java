package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostsOfTheDamned.class, DurkwoodBoars.class, Karakas.class})
class GhostsOfTheDamnedTest extends BaseCardTest {

    @Test
    @DisplayName("The ability can target itself and reduce power below zero")
    void canTargetItself() {
        Permanent ghosts = addCreatureReady(player1, new GhostsOfTheDamned());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, ghosts.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ghosts)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, ghosts)).isEqualTo(2);
        assertThat(ghosts.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Ghosts of the Damned");
    }

    @Test
    @DisplayName("Reductions from two Ghosts accumulate on the same creature")
    void reductionsAccumulate() {
        addCreatureReady(player1, new GhostsOfTheDamned());
        addCreatureReady(player1, new GhostsOfTheDamned());
        Permanent boars = addCreatureReady(player2, new DurkwoodBoars());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, boars.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, boars.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, boars)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, boars)).isEqualTo(4);
    }

    @Test
    @DisplayName("A summoning-sick Ghost cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent ghosts = harness.addToBattlefieldAndReturn(player1, new GhostsOfTheDamned());
        ghosts.setSummoningSick(true);
        Permanent boars = addCreatureReady(player2, new DurkwoodBoars());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, boars.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ghosts.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An already tapped Ghost cannot activate again")
    void cannotActivateWhileTapped() {
        Permanent ghosts = addCreatureReady(player1, new GhostsOfTheDamned());
        ghosts.tap();
        Permanent boars = addCreatureReady(player2, new DurkwoodBoars());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, boars.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tap ability gives target creature -1/-0 until end of turn")
    void shrinksTargetCreature() {
        Permanent ghosts = addCreatureReady(player1, new GhostsOfTheDamned());
        Permanent boars = addCreatureReady(player2, new DurkwoodBoars());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, boars.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, boars)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, boars)).isEqualTo(4);
        assertThat(ghosts.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability can target a creature its controller controls")
    void shrinksControlledCreature() {
        addCreatureReady(player1, new GhostsOfTheDamned());
        Permanent boars = addCreatureReady(player1, new DurkwoodBoars());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, boars.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, boars)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, boars)).isEqualTo(4);
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new GhostsOfTheDamned());
        Permanent boars = addCreatureReady(player2, new DurkwoodBoars());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, boars.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, boars)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, boars)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent ghosts = addCreatureReady(player1, new GhostsOfTheDamned());
        Permanent karakas = harness.addToBattlefieldAndReturn(player2, new Karakas());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, karakas.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ghosts.isTapped()).isFalse();
    }
}
