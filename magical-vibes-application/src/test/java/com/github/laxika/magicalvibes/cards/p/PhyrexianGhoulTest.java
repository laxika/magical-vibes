package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianGhoul.class, GorillaWarrior.class})
class PhyrexianGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature gives Phyrexian Ghoul +2/+2")
    void sacrificingCreatureBoostsPhyrexianGhoul() {
        Permanent ghoul = addCreatureReady(player1, new PhyrexianGhoul());
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, gorilla.getId());
        harness.passBothPriorities();

        assertThat(ghoul.getEffectivePower()).isEqualTo(4);
        assertThat(ghoul.getEffectiveToughness()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Gorilla Warrior");
    }

    @Test
    @DisplayName("Phyrexian Ghoul can sacrifice itself")
    void canSacrificeItself() {
        addCreatureReady(player1, new PhyrexianGhoul());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Phyrexian Ghoul");
        harness.assertInGraveyard(player1, "Phyrexian Ghoul");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent ghoul = addCreatureReady(player1, new PhyrexianGhoul());
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, gorilla.getId());
        harness.passBothPriorities();
        assertThat(ghoul.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ghoul.getEffectivePower()).isEqualTo(2);
        assertThat(ghoul.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple activations give cumulative boosts")
    void multipleActivationsGiveCumulativeBoosts() {
        Permanent ghoul = addCreatureReady(player1, new PhyrexianGhoul());
        Permanent firstGorilla = addCreatureReady(player1, new GorillaWarrior());
        Permanent secondGorilla = addCreatureReady(player1, new GorillaWarrior());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstGorilla.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, secondGorilla.getId());
        harness.passBothPriorities();

        assertThat(ghoul.getEffectivePower()).isEqualTo(6);
        assertThat(ghoul.getEffectiveToughness()).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The sacrifice is paid before the boost resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent ghoul = addCreatureReady(player1, new PhyrexianGhoul());
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, gorilla.getId());

        harness.assertNotOnBattlefield(player1, "Gorilla Warrior");
        harness.assertInGraveyard(player1, "Gorilla Warrior");
        assertThat(gd.stack).hasSize(1);
        assertThat(ghoul.getEffectivePower()).isEqualTo(2);
        assertThat(ghoul.getEffectiveToughness()).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(ghoul.getEffectivePower()).isEqualTo(4);
        assertThat(ghoul.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("A tapped Phyrexian Ghoul with summoning sickness can activate")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new PhyrexianGhoul());
        ghoul.setSummoningSick(true);
        ghoul.tap();
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, gorilla.getId());
        harness.passBothPriorities();

        assertThat(ghoul.getEffectivePower()).isEqualTo(4);
        assertThat(ghoul.getEffectiveToughness()).isEqualTo(4);
        assertThat(ghoul.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Gorilla Warrior");
    }

    @Test
    @DisplayName("Pending boosts do not affect another Ghoul after the source sacrifices itself")
    void pendingBoostsDoNotAffectAnotherGhoul() {
        Permanent source = addCreatureReady(player1, new PhyrexianGhoul());
        Permanent gorilla = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());
        Permanent otherGhoul = addCreatureReady(player1, new PhyrexianGhoul());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, gorilla.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, source.getId());

        harness.assertInGraveyard(player1, "Phyrexian Ghoul");
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(otherGhoul);
        assertThat(otherGhoul.getEffectivePower()).isEqualTo(2);
        assertThat(otherGhoul.getEffectiveToughness()).isEqualTo(2);
    }
}
