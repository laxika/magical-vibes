package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

        declareAttackers(player1, List.of(0));
        harness.beginBlockerDeclarationInput();
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
        toady.setLosesAllAbilitiesUntilEndOfTurn(true);

        assertThatCode(() -> declareAttackers(player1, List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot block when the creature counts are tied")
    void cannotBlockWhenCreatureCountsAreTied() {
        Permanent attacker = addCreatureReady(player1, new Mossdog());
        attacker.setAttacking(true);
        addCreatureReady(player2, new MoggToady());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block when controlling more creatures than attacking player")
    void canBlockWhenControllingMoreCreatures() {
        Permanent attacker = addCreatureReady(player1, new Mossdog());
        attacker.setAttacking(true);
        Permanent toady = addCreatureReady(player2, new MoggToady());
        addCreatureReady(player2, new Mossdog());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(toady.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block when attacking player controls more creatures")
    void cannotBlockWhenAttackingPlayerControlsMoreCreatures() {
        Permanent attacker = addCreatureReady(player1, new Mossdog());
        attacker.setAttacking(true);
        addCreatureReady(player1, new Mossdog());
        addCreatureReady(player2, new MoggToady());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block after losing all abilities even when creature counts are tied")
    void canBlockAfterLosingAllAbilities() {
        Permanent attacker = addCreatureReady(player1, new Mossdog());
        attacker.setAttacking(true);
        Permanent toady = addCreatureReady(player2, new MoggToady());
        toady.setLosesAllAbilitiesUntilEndOfTurn(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(toady.isBlocking()).isTrue();
    }
}
