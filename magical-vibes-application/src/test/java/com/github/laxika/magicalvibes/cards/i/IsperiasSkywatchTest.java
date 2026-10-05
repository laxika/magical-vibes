package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AxebaneGuardian;
import com.github.laxika.magicalvibes.cards.t.ToweringIndrik;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IsperiasSkywatch.class, ToweringIndrik.class, AxebaneGuardian.class})
class IsperiasSkywatchTest extends BaseCardTest {

    @Test
    @DisplayName("Detained creature can't attack")
    void detainedCreatureCannotAttack() {
        harness.addToBattlefield(player2, new ToweringIndrik());
        Permanent bears = detain("Towering Indrik");

        assertThatThrownBy(() -> declareAttack(bears))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Detained creature can't block")
    void detainedCreatureCannotBlock() {
        harness.addToBattlefield(player2, new ToweringIndrik());
        detain("Towering Indrik");

        Permanent attacker = addCreatureReady(player1, new ToweringIndrik());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Detained creature can't activate its abilities")
    void detainedCreatureCannotActivateAbilities() {
        addCreatureReady(player2, new AxebaneGuardian());
        detain("Axebane Guardian");

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Detain wears off at the Skywatch controller's next turn")
    void detainWearsOffAtControllersNextTurn() {
        harness.addToBattlefield(player2, new ToweringIndrik());
        Permanent bears = detain("Towering Indrik");

        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareAttack(bears)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new ToweringIndrik());
        UUID ownBearId = harness.getPermanentId(player1, "Towering Indrik");
        harness.setHand(player1, List.of(new IsperiasSkywatch()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, ownBearId, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Skywatch stays on the battlefield after the detain trigger resolves")
    void skywatchRemainsOnBattlefield() {
        harness.addToBattlefield(player2, new ToweringIndrik());
        detain("Towering Indrik");

        harness.assertOnBattlefield(player1, "Isperia's Skywatch");
    }

    @Test
    @DisplayName("Detain does not expire at the opponent's turn start")
    void detainPersistsDuringOpponentsTurn() {
        harness.addToBattlefield(player2, new ToweringIndrik());
        Permanent creature = detain("Towering Indrik");

        gd.expireFloatingEffectsAtTurnStart(player2.getId());

        assertThatThrownBy(() -> declareAttack(creature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Removing Skywatch in response does not stop its detain trigger")
    void detainResolvesAfterSkywatchLeavesBattlefield() {
        harness.addToBattlefield(player2, new ToweringIndrik());
        castSkywatch("Towering Indrik");
        harness.passBothPriorities();
        Permanent skywatch = findPermanent(player1, "Isperia's Skywatch");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, skywatch));

        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttack(findPermanent(player2, "Towering Indrik")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A creature that leaves and returns before resolution is not detained")
    void returnedCreatureIsANewTargetObject() {
        harness.addToBattlefield(player2, new ToweringIndrik());
        castSkywatch("Towering Indrik");
        harness.passBothPriorities();
        Permanent target = findPermanent(player2, "Towering Indrik");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, target));
        harness.setHand(player2, List.of());
        Permanent returned = addCreatureReady(player2, target.getCard());

        harness.passBothPriorities();

        assertThatCode(() -> declareAttack(returned)).doesNotThrowAnyException();
        harness.assertOnBattlefield(player1, "Isperia's Skywatch");
    }

    @Test
    @DisplayName("Skywatch can enter when there are no opposing creatures")
    void entersWithoutLegalTargets() {
        harness.castFromHand(player1, new IsperiasSkywatch(), "{5}{U}");
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Isperia's Skywatch");
    }

    private void castSkywatch(String targetName) {
        UUID targetId = harness.getPermanentId(player2, targetName);
        harness.setHand(player1, List.of(new IsperiasSkywatch()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }

    /** Casts the Skywatch targeting the named player2 creature and resolves both spell and trigger. */
    private Permanent detain(String targetName) {
        castSkywatch(targetName);
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
