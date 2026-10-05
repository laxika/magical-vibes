package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KitesailCorsair.class})
class KitesailCorsairTest extends BaseCardTest {

    private Permanent addCorsair() {
        Permanent corsair = addCreatureReady(player1, new KitesailCorsair());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return corsair;
    }

    @Test
    @DisplayName("Does not have flying while not attacking")
    void noFlyingWhileNotAttacking() {
        Permanent corsair = addCorsair();

        assertThat(gqs.hasKeyword(gd, corsair, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Has flying while attacking")
    void hasFlyingWhileAttacking() {
        Permanent corsair = addCorsair();

        corsair.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, corsair, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Loses flying when it stops attacking")
    void losesFlyingWhenNotAttacking() {
        Permanent corsair = addCorsair();
        corsair.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, corsair, Keyword.FLYING)).isTrue();

        corsair.setAttacking(false);

        assertThat(gqs.hasKeyword(gd, corsair, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An attacking Corsair cannot be blocked by a defending Corsair")
    void defendingCorsairCannotBlockAttackingCorsair() {
        Permanent attacker = addCreatureReady(player1, new KitesailCorsair());
        Permanent defender = addCreatureReady(player2, new KitesailCorsair());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, defender, Keyword.FLYING)).isFalse();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Attacking does not grant flying to another Corsair")
    void flyingAppliesOnlyToAttackingCorsair() {
        Permanent attacker = addCreatureReady(player1, new KitesailCorsair());
        Permanent nonAttacker = addCreatureReady(player1, new KitesailCorsair());
        addCreatureReady(player2, new KitesailCorsair());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Blocking does not give Corsair flying")
    void noFlyingWhileBlocking() {
        Permanent corsair = addCreatureReady(player2, new KitesailCorsair());
        corsair.setBlocking(true);

        assertThat(gqs.hasKeyword(gd, corsair, Keyword.FLYING)).isFalse();
    }
}
