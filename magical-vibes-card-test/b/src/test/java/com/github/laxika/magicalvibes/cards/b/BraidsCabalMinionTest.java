package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AetherBurst;
import com.github.laxika.magicalvibes.cards.c.CharmedPendant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.cards.w.Werebear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BraidsCabalMinion.class, CharmedPendant.class, Forest.class, GroundSeal.class, Werebear.class, AetherBurst.class})
class BraidsCabalMinionTest extends BaseCardTest {

    @Test
    @DisplayName("At each player's upkeep that player sacrifices an artifact, creature, or land")
    void activePlayerChoosesEligiblePermanentToSacrifice() {
        harness.addToBattlefield(player1, new BraidsCabalMinion());
        harness.addToBattlefield(player2, new CharmedPendant());
        harness.addToBattlefield(player2, new Werebear());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(land.getId()));

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Charmed Pendant");
        harness.assertOnBattlefield(player2, "Werebear");
    }

    @Test
    @DisplayName("A non-artifact, noncreature, nonland permanent is not eligible")
    void nonEligiblePermanentIsNotSacrificed() {
        harness.addToBattlefield(player1, new BraidsCabalMinion());
        harness.addToBattlefield(player2, new GroundSeal());
        harness.addToBattlefield(player2, new Werebear());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Werebear");
        harness.assertOnBattlefield(player2, "Ground Seal");
    }

    @Test
    @DisplayName("The controller also sacrifices a permanent during their own upkeep")
    void controllerIsAlsoAffected() {
        harness.addToBattlefield(player1, new BraidsCabalMinion());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Werebear());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));

        harness.assertOnBattlefield(player1, "Braids, Cabal Minion");
        harness.assertNotOnBattlefield(player1, "Werebear");
    }

    @Test
    @DisplayName("Does nothing when the active player controls no artifact, creature, or land")
    void noEligiblePermanentDoesNotPromptOrSacrifice() {
        harness.addToBattlefield(player1, new BraidsCabalMinion());
        harness.addToBattlefield(player2, new GroundSeal());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Ground Seal");
    }

    @Test
    @DisplayName("The active player can choose an artifact instead of a creature or land")
    void activePlayerCanChooseArtifact() {
        harness.addToBattlefield(player1, new BraidsCabalMinion());
        harness.addToBattlefield(player2, new CharmedPendant());
        harness.addToBattlefield(player2, new Werebear());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(harness.getPermanentId(player2, "Charmed Pendant")));

        harness.assertInGraveyard(player2, "Charmed Pendant");
        harness.assertNotOnBattlefield(player2, "Charmed Pendant");
        harness.assertOnBattlefield(player2, "Werebear");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Braids can sacrifice herself on her controller's upkeep")
    void controllerCanChooseBraidsHerself() {
        harness.addToBattlefield(player1, new BraidsCabalMinion());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(harness.getPermanentId(player1, "Braids, Cabal Minion")));

        harness.assertInGraveyard(player1, "Braids, Cabal Minion");
        harness.assertNotOnBattlefield(player1, "Braids, Cabal Minion");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Removing Braids in response does not stop the upkeep sacrifice")
    void triggerResolvesAfterBraidsLeavesBattlefield() {
        harness.addToBattlefield(player1, new BraidsCabalMinion());
        harness.addToBattlefield(player2, new Werebear());
        harness.setHand(player2, List.of(new AetherBurst()));

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, List.of(harness.getPermanentId(player1, "Braids, Cabal Minion")));

        harness.assertInHand(player1, "Braids, Cabal Minion");
        harness.assertOnBattlefield(player2, "Werebear");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Werebear");
        harness.assertNotOnBattlefield(player2, "Werebear");
    }
}
