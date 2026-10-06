package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShatterskullRecruit.class})
class ShatterskullRecruitTest extends BaseCardTest {

    @Test
    @DisplayName("Menace prevents Shatterskull Recruit from being blocked by one creature")
    void cannotBeBlockedByOneCreature() {
        addCreatureReady(player1, new ShatterskullRecruit());
        addCreatureReady(player2, new ShatterskullRecruit());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace allows Shatterskull Recruit to be blocked by two creatures")
    void canBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new ShatterskullRecruit());
        Permanent firstBlocker = addCreatureReady(player2, new ShatterskullRecruit());
        Permanent secondBlocker = addCreatureReady(player2, new ShatterskullRecruit());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Menace allows Shatterskull Recruit to be blocked by more than two creatures")
    void canBeBlockedByThreeCreatures() {
        addCreatureReady(player1, new ShatterskullRecruit());
        Permanent firstBlocker = addCreatureReady(player2, new ShatterskullRecruit());
        Permanent secondBlocker = addCreatureReady(player2, new ShatterskullRecruit());
        Permanent thirdBlocker = addCreatureReady(player2, new ShatterskullRecruit());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
        assertThat(thirdBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Menace does not require the defending player to block")
    void canRemainUnblocked() {
        addCreatureReady(player1, new ShatterskullRecruit());
        Permanent firstBlocker = addCreatureReady(player2, new ShatterskullRecruit());
        Permanent secondBlocker = addCreatureReady(player2, new ShatterskullRecruit());
        int startingLife = gd.playerLifeTotals.get(player2.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(firstBlocker.isBlocking()).isFalse();
        assertThat(secondBlocker.isBlocking()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife - 4);
    }
}
