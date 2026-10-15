package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BumbleflowersSharepot.class, Forest.class, GrizzlyBears.class})
class BumbleflowersSharepotTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token when it enters")
    void createsFoodOnEnter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BumbleflowersSharepot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bumbleflower's Sharepot");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("Sacrifices itself and destroys a target nonland permanent")
    void sacrificesItselfAndDestroysNonlandPermanent() {
        addReadySharepot(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bumbleflower's Sharepot");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addReadySharepot(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    @DisplayName("Can activate only as a sorcery")
    void canActivateOnlyAsSorcery() {
        addReadySharepot(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadySharepot(Player player) {
        return addCreatureReady(player, new BumbleflowersSharepot());
    }

    @Test
    @DisplayName("The created Food can be sacrificed immediately for 3 life")
    void createdFoodGainsLife() {
        harness.setHand(player1, List.of(new BumbleflowersSharepot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
        harness.assertOnBattlefield(player1, "Bumbleflower's Sharepot");
    }

    @Test
    @DisplayName("Can destroy an opposing artifact and is sacrificed before resolution")
    void destroysArtifact() {
        addReadySharepot(player1);
        harness.addToBattlefield(player2, new BumbleflowersSharepot());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Bumbleflower's Sharepot"));

        harness.assertInGraveyard(player1, "Bumbleflower's Sharepot");
        harness.assertOnBattlefield(player2, "Bumbleflower's Sharepot");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Bumbleflower's Sharepot");
    }

    @Test
    @DisplayName("Can target itself even though sacrificing it makes the target illegal")
    void canTargetItself() {
        Permanent sharepot = addReadySharepot(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, sharepot.getId());

        harness.assertInGraveyard(player1, "Bumbleflower's Sharepot");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Bumbleflower's Sharepot");
    }

    @Test
    @DisplayName("Cannot activate during a main phase while a spell is on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent sharepot = addReadySharepot(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BumbleflowersSharepot()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castArtifact(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sharepot.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.assertOnBattlefield(player1, "Bumbleflower's Sharepot");
    }
}
