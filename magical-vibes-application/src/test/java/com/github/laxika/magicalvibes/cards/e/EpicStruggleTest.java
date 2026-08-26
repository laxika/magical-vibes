package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EpicStruggle.class, GrizzlyBears.class})
class EpicStruggleTest extends BaseCardTest {

    @Test
    @DisplayName("Wins the game at upkeep with exactly 20 creatures")
    void winsWithExactlyTwentyCreatures() {
        harness.addToBattlefield(player1, new EpicStruggle());
        addCreatures(player1, 20);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger at upkeep with only 19 creatures")
    void doesNotTriggerWithNineteenCreatures() {
        harness.addToBattlefield(player1, new EpicStruggle());
        addCreatures(player1, 19);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Creatures controlled by an opponent do not count")
    void opponentCreaturesDoNotCount() {
        harness.addToBattlefield(player1, new EpicStruggle());
        addCreatures(player1, 10);
        addCreatures(player2, 10);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Rechecks the creature count when the trigger resolves")
    void rechecksCreatureCountOnResolution() {
        harness.addToBattlefield(player1, new EpicStruggle());
        addCreatures(player1, 20);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    private void addCreatures(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent creature = new Permanent(new GrizzlyBears());
            creature.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(creature);
        }
    }
}
