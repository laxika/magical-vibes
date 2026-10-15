package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HopeOfGhirapur.class, Shock.class, DruidOfTheCowl.class})
class HopeOfGhirapurTest extends BaseCardTest {

    private void activateHopeAgainstPlayer2() {
        addCreatureReady(player1, new HopeOfGhirapur());
        declareAttackers(List.of(0));
        resolveCombat();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Hope of Ghirapur prevents the damaged player from casting noncreature spells")
    void preventsNoncreatureSpells() {
        activateHopeAgainstPlayer2();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Hope of Ghirapur still allows the damaged player to cast creature spells")
    void allowsCreatureSpells() {
        activateHopeAgainstPlayer2();

        harness.setHand(player2, List.of(new DruidOfTheCowl()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Druid of the Cowl");
    }

    @Test
    @DisplayName("Hope of Ghirapur's restriction ends at its controller's next turn")
    void restrictionEndsAtControllerNextTurn() {
        activateHopeAgainstPlayer2();
        harness.setHand(player2, List.of(new Shock()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Hope of Ghirapur cannot target a player it did not damage in combat")
    void cannotTargetUndamagedPlayer() {
        addCreatureReady(player1, new HopeOfGhirapur());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and the damaged player can respond before resolution")
    void sacrificeIsCostAndRestrictionStartsOnResolution() {
        addCreatureReady(player1, new HopeOfGhirapur());
        declareAttackers(List.of(0));
        resolveCombat();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertNotOnBattlefield(player1, "Hope of Ghirapur");
        harness.assertInGraveyard(player1, "Hope of Ghirapur");

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activating player can still cast noncreature spells")
    void restrictionAppliesOnlyToTargetedPlayer() {
        activateHopeAgainstPlayer2();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Damage dealt by a different Hope of Ghirapur does not make a player a legal target")
    void anotherHopeCannotUseFirstHopesDamage() {
        activateHopeAgainstPlayer2();
        harness.addToBattlefield(player1, new HopeOfGhirapur());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Hope of Ghirapur");
    }

    @Test
    @DisplayName("Combat damage from a previous turn does not make a player a legal target")
    void previousTurnsCombatDamageDoesNotQualify() {
        addCreatureReady(player1, new HopeOfGhirapur());
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Hope of Ghirapur");
    }

    @Test
    @DisplayName("The restriction remains in effect during the damaged player's next turn")
    void restrictionPersistsThroughTargetsTurn() {
        activateHopeAgainstPlayer2();
        harness.setHand(player2, List.of(new Shock()));
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact creature spells remain legal under the restriction")
    void allowsArtifactCreatureSpells() {
        activateHopeAgainstPlayer2();
        harness.setHand(player2, List.of(new HopeOfGhirapur()));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hope of Ghirapur");
    }
}
