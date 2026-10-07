package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TauntFromTheRampart.class, GrizzlyBears.class})
class TauntFromTheRampartTest extends BaseCardTest {

    @Test
    @DisplayName("Goads each opposing creature until the controller's next turn")
    void goadsOpposingCreatures() {
        addCreatureReady(player2, new GrizzlyBears());
        castTaunt();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Goaded opposing creatures cannot block until the controller's next turn")
    void opposingCreaturesCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        castTaunt();

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Creatures entering after the spell resolves are not goaded")
    void laterCreaturesAreNotGoaded() {
        castTaunt();
        addCreatureReady(player2, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Goad and the blocking restriction expire at the controller's next turn")
    void restrictionsExpireAtControllersNextTurn() {
        addCreatureReady(player2, new GrizzlyBears());
        castTaunt();
        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareAttackers(player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Creatures entering after resolution can still block")
    void laterCreaturesCanBlock() {
        castTaunt();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The caster's own creatures are not goaded")
    void ownCreaturesAreNotGoaded() {
        addCreatureReady(player1, new GrizzlyBears());
        castTaunt();

        assertThatCode(() -> declareAttackers(player1, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The caster's own creatures can still block")
    void ownCreaturesCanBlock() {
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        castTaunt();
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatCode(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker)))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The blocking restriction expires at the caster's next turn")
    void blockingRestrictionExpires() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castTaunt();
        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Starting the opponent's turn does not expire goad")
    void opponentsTurnDoesNotExpireGoad() {
        addCreatureReady(player2, new GrizzlyBears());
        castTaunt();
        gd.expireFloatingEffectsAtTurnStart(player2.getId());

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A tapped goaded creature is not required to attack")
    void tappedCreatureNeedNotAttack() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        castTaunt();
        creature.tap();

        assertThatCode(() -> declareAttackers(player2, List.of()))
                .doesNotThrowAnyException();
    }

    private void castTaunt() {
        harness.setHand(player1, List.of(new TauntFromTheRampart()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
