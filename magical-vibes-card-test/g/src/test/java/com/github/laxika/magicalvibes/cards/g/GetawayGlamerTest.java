package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GetawayGlamer.class, GrizzlyBears.class, HillGiant.class})
class GetawayGlamerTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers a nontoken creature until the next end step")
    void flickersNontokenCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        cast(new int[]{0}, List.of(target.getId()), 2);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Flicker mode rejects a token creature")
    void flickerModeRejectsTokenCreature() {
        Permanent token = harness.addToBattlefieldAndReturn(player2, token("Soldier Token"));

        harness.setHand(player1, List.of(new GetawayGlamer()));
        addMana(2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(token.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken creature");
    }

    @Test
    @DisplayName("Destroy mode destroys a creature tied for greatest power")
    void destroyModeDestroysGreatestPowerCreature() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        addCreatureReady(player1, new HillGiant());

        cast(new int[]{1}, List.of(target.getId()), 3);

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Destroy mode does nothing when another creature has greater power")
    void destroyModeDoesNothingForLowerPowerCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new HillGiant());

        cast(new int[]{1}, List.of(target.getId()), 3);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Both modes check greatest power after exiling the stronger creature")
    void bothModesExileBeforeCheckingPower() {
        Permanent stronger = addCreatureReady(player1, new HillGiant());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        cast(new int[]{0, 1}, List.of(stronger.getId(), target.getId()), 4);

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Both modes require paying both additional costs")
    void bothModesRejectInsufficientMana() {
        Permanent firstTarget = addCreatureReady(player1, new HillGiant());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GetawayGlamer()));
        addMana(3);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(firstTarget.getId(), secondTarget.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroy mode can destroy a token when it is the only creature")
    void destroyModeCanDestroyToken() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, token("Soldier Token"));

        cast(new int[]{1}, List.of(target.getId()), 3);

        harness.assertNotOnBattlefield(player2, "Soldier Token");
    }

    @Test
    @DisplayName("Both modes may target the same creature, which returns instead of being destroyed")
    void bothModesMayShareTarget() {
        Permanent target = addCreatureReady(player2, new HillGiant());

        cast(new int[]{0, 1}, List.of(target.getId(), target.getId()), 4);

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Hill Giant");

        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Destroy mode rechecks power when a stronger creature enters before resolution")
    void destroyModeRechecksPowerAtResolution() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GetawayGlamer()));
        addMana(3);
        harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{1}, List.of(target.getId()));
        addCreatureReady(player1, new HillGiant());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Flicker returns a stolen creature to its owner rather than its controller")
    void flickerReturnsToOwner() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        gd.stolenCreatures.put(target.getId(), player2.getId());

        cast(new int[]{0}, List.of(target.getId()), 2);

        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting during an end step returns the creature at the following end step")
    void flickerDuringEndStepWaitsForNextEndStep() {
        harness.forceStep(TurnStep.END_STEP);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(target.getId()), 2);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void cast(int[] modes, List<UUID> targets, int totalMana) {
        harness.setHand(player1, List.of(new GetawayGlamer()));
        addMana(totalMana);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targets);
        harness.passBothPriorities();
    }

    private void addMana(int totalMana) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - 1);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private static Card token(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
