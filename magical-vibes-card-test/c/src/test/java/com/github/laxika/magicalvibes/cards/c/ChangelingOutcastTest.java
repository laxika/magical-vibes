package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoblinMatron;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChangelingOutcast.class, GrizzlyBears.class, GoblinMatron.class})
class ChangelingOutcastTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling Outcast can't be blocked")
    void cannotBeBlocked() {
        addCreatureReady(player1, new ChangelingOutcast());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Changeling Outcast can't block")
    void cannotBlock() {
        addCreatureReady(player2, new ChangelingOutcast());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Changeling Outcast is a Goblin card in the library")
    void canBeFoundByGoblinMatron() {
        ChangelingOutcast outcast = new ChangelingOutcast();
        harness.setLibrary(player1, List.of(outcast));
        harness.castFromHand(player1, new GoblinMatron(), "{2}{R}");

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(outcast);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(outcast);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(outcast);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
