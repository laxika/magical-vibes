package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.v.VenerableMonk;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfRazors.class, VenerableMonk.class, YouthfulKnight.class})
class WallOfRazorsTest extends BaseCardTest {

    @Test
    @DisplayName("Wall of Razors cannot attack while it has defender")
    void cannotAttack() {
        addCreatureReady(player1, new WallOfRazors());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wall of Razors deals first-strike damage before a blocked attacker")
    void dealsFirstStrikeDamageBeforeBlockedAttacker() {
        addCreatureReady(player1, new VenerableMonk());
        addCreatureReady(player2, new WallOfRazors());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Venerable Monk");
        harness.assertOnBattlefield(player2, "Wall of Razors");
    }

    @Test
    @DisplayName("Wall of Razors and a first-strike attacker deal damage simultaneously")
    void tradesWithFirstStrikeAttacker() {
        addCreatureReady(player1, new YouthfulKnight());
        addCreatureReady(player2, new WallOfRazors());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Youthful Knight");
        harness.assertInGraveyard(player2, "Wall of Razors");
        harness.assertNotOnBattlefield(player1, "Youthful Knight");
        harness.assertNotOnBattlefield(player2, "Wall of Razors");
    }
}
