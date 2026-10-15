package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinWarCry.class, BearCub.class, Island.class})
class GoblinWarCryTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen creature can still block; the target opponent's other creatures can't")
    void chosenCreatureCanBlockOthersCant() {
        Permanent kept = addReadyCreature(player2);
        Permanent other = addReadyCreature(player2);

        castGoblinWarCry();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        assertThat(kept.isCantBlockThisTurn()).isFalse();
        assertThat(other.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Every creature except the chosen one can't block")
    void allButChosenCantBlock() {
        Permanent kept = addReadyCreature(player2);
        Permanent other1 = addReadyCreature(player2);
        Permanent other2 = addReadyCreature(player2);

        castGoblinWarCry();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        assertThat(kept.isCantBlockThisTurn()).isFalse();
        assertThat(other1.isCantBlockThisTurn()).isTrue();
        assertThat(other2.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Only creatures are considered; other permanents are unaffected")
    void onlyCreaturesAreRestricted() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent kept = addReadyCreature(player2);
        Permanent other = addReadyCreature(player2);

        castGoblinWarCry();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        assertThat(island.isCantBlockThisTurn()).isFalse();
        assertThat(kept.isCantBlockThisTurn()).isFalse();
        assertThat(other.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A lone creature is never restricted (no other creatures)")
    void singleCreatureNotRestricted() {
        Permanent only = addReadyCreature(player2);

        castGoblinWarCry();
        harness.handleMultiplePermanentsChosen(player2, List.of(only.getId()));

        assertThat(only.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Resolves harmlessly when the target opponent controls no creatures")
    void noCreaturesResolvesHarmlessly() {
        castGoblinWarCry();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The controller's own creatures are unaffected")
    void controllersCreaturesUnaffected() {
        Permanent own = addReadyCreature(player1);
        Permanent kept = addReadyCreature(player2);
        Permanent other = addReadyCreature(player2);

        castGoblinWarCry();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        assertThat(own.isCantBlockThisTurn()).isFalse();
        assertThat(other.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A restricted creature can't be declared as a blocker")
    void restrictedCreatureCantBlock() {
        addReadyCreature(player1);
        Permanent kept = addReadyCreature(player2);
        addReadyCreature(player2); // the restricted blocker (index 1)

        castGoblinWarCry();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can't target the caster's own player")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new GoblinWarCry()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The opponent must choose a creature when they control one")
    void cannotDeclineCreatureChoice() {
        Permanent kept = addReadyCreature(player2);
        addReadyCreature(player2);

        castGoblinWarCry();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));
    }

    @Test
    @DisplayName("The chosen creature can legally block")
    void chosenCreatureCanBeDeclaredAsBlocker() {
        addReadyCreature(player1);
        Permanent kept = addReadyCreature(player2);
        addReadyCreature(player2);

        castGoblinWarCry();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Creatures entering after the choice also can't block")
    void laterCreatureCantBlock() {
        addReadyCreature(player1);
        Permanent kept = addReadyCreature(player2);

        castGoblinWarCry();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));
        harness.addToBattlefield(player2, new BearCub());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An initially empty battlefield does not exempt creatures entering later")
    void laterCreatureCantBlockWhenOpponentInitiallyHadNone() {
        addReadyCreature(player1);

        castGoblinWarCry();
        harness.addToBattlefield(player2, new BearCub());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The restriction expires at the end of the turn")
    void restrictionExpiresAtEndOfTurn() {
        Permanent kept = addReadyCreature(player2);
        Permanent other = addReadyCreature(player2);

        castGoblinWarCry();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(other.isCantBlockThisTurn()).isFalse();
        addReadyCreature(player1);
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @CardUsed({RayOfCommand.class})
    @DisplayName("A creature can block after leaving the targeted opponent's control")
    void restrictionDoesNotFollowCreatureToAnotherController() {
        Permanent kept = addReadyCreature(player2);
        Permanent other = addReadyCreature(player2);

        castGoblinWarCry();
        harness.handleMultiplePermanentsChosen(player2, List.of(kept.getId()));
        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0, other.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(other);
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    private void castGoblinWarCry() {
        harness.setHand(player1, List.of(new GoblinWarCry()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new BearCub());
    }
}
