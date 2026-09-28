package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AinokStrikeLeader.class, GrizzlyBears.class})
class AinokStrikeLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Ainok Strike Leader creates a tapped and attacking Goblin")
    void leaderAttackCreatesGoblin() {
        addCreatureReady(player1, new AinokStrikeLeader());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        Permanent goblin = findPermanents(player1, "Goblin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(goblin.isTapped()).isTrue();
        assertThat(goblin.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Attacking with your commander creates a Goblin even if the leader stays back")
    void commanderAttackCreatesGoblin() {
        addCreatureReady(player1, new AinokStrikeLeader());
        Permanent commander = addCreatureReady(player1, new GrizzlyBears());
        gd.makeCommander(player1.getId(), commander.getCard());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("An unrelated creature attack does not trigger Ainok Strike Leader")
    void unrelatedAttackDoesNotCreateGoblin() {
        addCreatureReady(player1, new AinokStrikeLeader());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing the leader grants indestructible to your creature tokens until end of turn")
    void sacrificeProtectsCreatureTokens() {
        Permanent leader = addCreatureReady(player1, new AinokStrikeLeader());
        Permanent nonToken = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Goblin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(leader), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonToken, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertInGraveyard(player1, "Ainok Strike Leader");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
