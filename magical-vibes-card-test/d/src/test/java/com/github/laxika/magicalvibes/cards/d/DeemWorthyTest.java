package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeemWorthy.class, GrizzlyBears.class, SerraAngel.class})
class DeemWorthyTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 7 damage to target creature, destroying a 4/4")
    void deals7ToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new DeemWorthy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Main spell cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // a legal creature target exists
        harness.setHand(player1, List.of(new DeemWorthy()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling deals 2 damage to target creature, destroying a 2/2, and draws a card")
    void cyclingDeals2ToCreatureAndDraws() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeemWorthy()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        addCyclingMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        cycleWithDamageTarget(targetId);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Serra Angel");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Deem Worthy");
        harness.assertInHand(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Cycling with no creatures available still draws")
    void cyclingWithoutTargetStillDraws() {
        harness.setHand(player1, List.of(new DeemWorthy()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        // The cycling draw still resolves.
        harness.assertInHand(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Losing the cycling damage target does not stop the separate draw ability")
    void cyclingStillDrawsWhenDamageTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeemWorthy()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        harness.setHand(player2, List.of(new DeemWorthy()));
        addCyclingMana(player1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        cycleWithDamageTarget(targetId);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Deem Worthy");
        harness.assertInHand(player1, "Serra Angel");
    }

    @Test
    @DisplayName("The cycling damage may be declined when its trigger resolves")
    void cyclingDamageCanBeDeclinedAtResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeemWorthy()));
        harness.setLibrary(player1, List.of(new SerraAngel()));
        addCyclingMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        cycleWithDamageTarget(targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Serra Angel");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Serra Angel");
    }

    private void cycleWithDamageTarget(UUID targetId) {
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
    }

    private void addCyclingMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.addMana(player, ManaColor.RED, 1);
    }
}
