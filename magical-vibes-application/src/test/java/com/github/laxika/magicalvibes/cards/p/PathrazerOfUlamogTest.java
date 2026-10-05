package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BeastbreakerOfBalaGed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PathrazerOfUlamog.class, GrizzlyBears.class, BeastbreakerOfBalaGed.class, PawnOfUlamog.class})
class PathrazerOfUlamogTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes the defending player sacrifice three permanents")
    void annihilatorThree() {
        Permanent pathrazer = addCreatureReady(player1, new PathrazerOfUlamog());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(pathrazer)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot be blocked by fewer than three creatures")
    void cannotBeBlockedByFewerThanThreeCreatures() {
        Permanent attacker = addCreatureReady(player1, new PathrazerOfUlamog());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by 3 or more creatures");
    }

    @Test
    void cannotBeBlockedByTwoCreatures() {
        Permanent attacker = addCreatureReady(player1, new PathrazerOfUlamog());
        attacker.setAttacking(true);
        addCreatureReady(player2, new BeastbreakerOfBalaGed());
        addCreatureReady(player2, new BeastbreakerOfBalaGed());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by 3 or more creatures");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 3, 4})
    void acceptsLegalBlockerCounts(int count) {
        Permanent attacker = addCreatureReady(player1, new PathrazerOfUlamog());
        attacker.setAttacking(true);
        for (int i = 0; i < count; i++) {
            addCreatureReady(player2, new BeastbreakerOfBalaGed());
        }
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new BlockerAssignment(i, 0)).toList());
    }

    @Test
    void annihilatorSacrificesAllWhenDefenderHasFewerThanThreePermanents() {
        addCreatureReady(player1, new PathrazerOfUlamog());
        harness.addToBattlefield(player2, new BeastbreakerOfBalaGed());
        harness.addToBattlefield(player2, new BeastbreakerOfBalaGed());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void defenderChoosesExactlyThreePermanentsToSacrifice() {
        addCreatureReady(player1, new PathrazerOfUlamog());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new BeastbreakerOfBalaGed());
        List<Permanent> sacrificed = java.util.stream.IntStream.range(0, 3)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new BeastbreakerOfBalaGed()))
                .toList();

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2,
                sacrificed.stream().map(Permanent::getId).toList());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void sacrificedPawnSeesAllThreeSimultaneousDeaths() {
        addCreatureReady(player1, new PathrazerOfUlamog());
        harness.addToBattlefield(player2, new PawnOfUlamog());
        harness.addToBattlefield(player2, new BeastbreakerOfBalaGed());
        harness.addToBattlefield(player2, new BeastbreakerOfBalaGed());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player2, true);
            resolveAllTriggers();
        }

        assertThat(findPermanents(player2, "Eldrazi Spawn")).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }
}
