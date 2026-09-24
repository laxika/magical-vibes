package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnucklesTheEchidna.class, LeoninScimitar.class})
class KnucklesTheEchidnaTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Treasure for each combat damage step that reaches a player")
    void createsTreasureForBothDoubleStrikeDamageSteps() {
        addCreatureReady(player1, new KnucklesTheEchidna());

        declareAttackers(List.of(0));
        resolveCombatUnblocked();

        assertThat(treasureCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Wins the game at upkeep while controlling thirty artifacts")
    void winsWithThirtyArtifacts() {
        harness.addToBattlefield(player1, new KnucklesTheEchidna());
        addArtifacts(player1, 30);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger at upkeep with only twenty-nine artifacts")
    void doesNotTriggerWithTwentyNineArtifacts() {
        harness.addToBattlefield(player1, new KnucklesTheEchidna());
        addArtifacts(player1, 29);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Opponent's artifacts do not count toward the win condition")
    void opponentArtifactsDoNotCount() {
        harness.addToBattlefield(player1, new KnucklesTheEchidna());
        addArtifacts(player1, 15);
        addArtifacts(player2, 15);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    private void resolveCombatUnblocked() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private long treasureCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(perm -> perm.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .count();
    }

    private void addArtifacts(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addPermanent(player, new LeoninScimitar());
        }
    }

    private Permanent addPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
