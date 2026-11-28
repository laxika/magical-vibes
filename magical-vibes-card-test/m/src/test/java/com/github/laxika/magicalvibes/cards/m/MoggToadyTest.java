package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoggToady.class, Mossdog.class})
class MoggToadyTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack when the creature counts are tied")
    void cannotAttackWhenCreatureCountsAreTied() {
        addCreatureReady(player1, new MoggToady());
        addCreatureReady(player2, new Mossdog());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when controlling more creatures than defending player")
    void canAttackWhenControllingMoreCreatures() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MoggToady());
        addCreatureReady(player1, new Mossdog());
        addCreatureReady(player2, new Mossdog());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls more creatures")
    void cannotAttackWhenDefendingPlayerControlsMoreCreatures() {
        addCreatureReady(player1, new MoggToady());
        addCreatureReady(player2, new Mossdog());
        addCreatureReady(player2, new Mossdog());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack after losing all abilities even when creature counts are tied")
    void canAttackAfterLosingAllAbilities() {
        Permanent toady = addCreatureReady(player1, new MoggToady());
        addCreatureReady(player2, new Mossdog());
        PermanentGrants.loseAllAbilitiesUntilEndOfTurn(gd, toady);

        assertThatCode(() -> declareAttackers(player1, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot block when the creature counts are tied")
    void cannotBlockWhenCreatureCountsAreTied() {
        addCreatureReady(player1, new Mossdog());
        addCreatureReady(player2, new MoggToady());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block when controlling more creatures than attacking player")
    void canBlockWhenControllingMoreCreatures() {
        addCreatureReady(player1, new Mossdog());
        Permanent toady = addCreatureReady(player2, new MoggToady());
        addCreatureReady(player2, new Mossdog());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(toady.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block when attacking player controls more creatures")
    void cannotBlockWhenAttackingPlayerControlsMoreCreatures() {
        addCreatureReady(player1, new Mossdog());
        addCreatureReady(player1, new Mossdog());
        addCreatureReady(player2, new MoggToady());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block after losing all abilities even when creature counts are tied")
    void canBlockAfterLosingAllAbilities() {
        addCreatureReady(player1, new Mossdog());
        Permanent toady = addCreatureReady(player2, new MoggToady());
        PermanentGrants.loseAllAbilitiesUntilEndOfTurn(gd, toady);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(toady.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped nonattacking creatures count toward the attack restriction")
    void tappedCreaturesCountTowardAttackRestriction() {
        addCreatureReady(player1, new MoggToady());
        Permanent support = addCreatureReady(player1, new Mossdog());
        support.tap();
        addCreatureReady(player2, new Mossdog());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures count toward the defending player's creature total")
    void tappedDefendingCreaturesCountTowardAttackRestriction() {
        addCreatureReady(player1, new MoggToady());
        Permanent defender = addCreatureReady(player2, new Mossdog());
        defender.tap();

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tapped creatures count toward the blocker's creature total")
    void tappedCreaturesCountTowardBlockRestriction() {
        addCreatureReady(player1, new Mossdog());
        Permanent toady = addCreatureReady(player2, new MoggToady());
        Permanent support = addCreatureReady(player2, new Mossdog());
        support.tap();

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(toady.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Nonattacking creatures count toward the attacking player's creature total")
    void nonattackingCreaturesCountTowardBlockRestriction() {
        addCreatureReady(player1, new Mossdog());
        Permanent support = addCreatureReady(player1, new Mossdog());
        support.tap();
        addCreatureReady(player2, new MoggToady());
        addCreatureReady(player2, new Mossdog());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With two opponents, it can attack only the one it has more creatures than")
    void attackIsJudgedForEachDefendingPlayer() {
        Player player3 = harness.addOpponent("Charlie");
        addCreatureReady(player1, new MoggToady());
        addCreatureReady(player1, new Mossdog());
        addCreatureReady(player3, new Mossdog());
        addCreatureReady(player3, new Mossdog());
        addCreatureReady(player3, new Mossdog());
        prepareAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0), Map.of(0, player3.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of(0), Map.of(0, player2.getId())));
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getAttackTarget())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("With two opponents, it can't block when the attacking player controls more creatures")
    void blockIsJudgedAgainstTheAttackingPlayer() {
        Player player3 = harness.addOpponent("Charlie");
        addCreatureReady(player3, new Mossdog());
        addCreatureReady(player3, new Mossdog());
        addCreatureReady(player3, new Mossdog());
        addCreatureReady(player1, new MoggToady());
        addCreatureReady(player1, new Mossdog());
        prepareAttack(player3);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player3, List.of(0), Map.of(0, player1.getId())));
        prepareDeclareBlockers(player3);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareAttack(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
