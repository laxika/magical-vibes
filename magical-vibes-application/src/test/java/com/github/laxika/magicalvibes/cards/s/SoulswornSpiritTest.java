package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AxebaneGuardian;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulswornSpirit.class, DrudgeBeetle.class, AxebaneGuardian.class})
class SoulswornSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Soulsworn Spirit cannot be blocked")
    void cannotBeBlocked() {
        addCreatureReady(player2, new DrudgeBeetle());

        Permanent spirit = addCreatureReady(player1, new SoulswornSpirit());
        spirit.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Detained creature can't attack")
    void detainedCreatureCannotAttack() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        Permanent beetle = detain("Drudge Beetle");

        assertThatThrownBy(() -> declareAttack(beetle))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Detained creature can't activate its abilities")
    void detainedCreatureCannotActivateAbilities() {
        addCreatureReady(player2, new AxebaneGuardian());
        detain("Axebane Guardian");

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Detain wears off at the Spirit controller's next turn")
    void detainWearsOffAtControllersNextTurn() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        Permanent beetle = detain("Drudge Beetle");

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareAttack(beetle)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new DrudgeBeetle());
        UUID ownBeetleId = harness.getPermanentId(player1, "Drudge Beetle");
        harness.setHand(player1, List.of(new SoulswornSpirit()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBeetleId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Detained creature cannot block a blockable attacker")
    void detainedCreatureCannotBlock() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        detain("Drudge Beetle");
        Permanent attacker = addCreatureReady(player1, new DrudgeBeetle());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Detain does not expire on the detained creature controller's turn")
    void detainPersistsThroughOpponentsTurnStart() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        Permanent beetle = detain("Drudge Beetle");

        gd.expireFloatingEffectsAtTurnStart(player2.getId());

        assertThatThrownBy(() -> declareAttack(beetle))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Detain continues after Soulsworn Spirit leaves the battlefield")
    void detainPersistsAfterSourceLeaves() {
        harness.addToBattlefield(player2, new DrudgeBeetle());
        Permanent beetle = detain("Drudge Beetle");
        Permanent spirit = findPermanent(player1, "Soulsworn Spirit");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spirit));

        assertThatThrownBy(() -> declareAttack(beetle))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Soulsworn Spirit can enter when no opponent controls a creature")
    void canEnterWithoutLegalDetainTarget() {
        harness.addToBattlefield(player1, new DrudgeBeetle());
        harness.setHand(player1, List.of(new SoulswornSpirit()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Soulsworn Spirit");
    }

    @Test
    @DisplayName("Detain does not tap the targeted creature")
    void detainDoesNotTapCreature() {
        harness.addToBattlefield(player2, new DrudgeBeetle());

        Permanent beetle = detain("Drudge Beetle");

        assertThat(beetle.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature returning before detain resolves is a new object")
    void returningCreatureIsNotDetainedByOldTrigger() {
        Permanent original = addCreatureReady(player2, new DrudgeBeetle());
        harness.setHand(player1, List.of(new SoulswornSpirit()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, original.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, original));
        Permanent returned = addCreatureReady(player2, original.getCard());
        harness.setHand(player2, List.of());
        resolveAllTriggers();

        assertThatCode(() -> declareAttack(returned)).doesNotThrowAnyException();
    }

    /** Casts the Spirit targeting the named player2 creature and resolves both spell and trigger. */
    private Permanent detain(String targetName) {
        UUID targetId = harness.getPermanentId(player2, targetName);
        harness.setHand(player1, List.of(new SoulswornSpirit()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        gs.playCard(gd, player1, 0, 0, targetId, null);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
        return findPermanent(player2, targetName);
    }

    /** Attempts to declare the given player2 creature as an attacker. */
    private void declareAttack(Permanent creature) {
        creature.setSummoningSick(false);
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(creature);
        declareAttackers(player2, List.of(index));
    }
}
