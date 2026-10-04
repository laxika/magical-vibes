package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnslavedScout.class, Mountain.class})
class EnslavedScoutTest extends BaseCardTest {

    @Test
    void canActivateWhileSummoningSick() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new EnslavedScout());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scout, Keyword.MOUNTAINWALK)).isTrue();
    }

    @Test
    @DisplayName("Gains mountainwalk until end of turn")
    void gainsMountainwalk() {
        Permanent scout = addCreatureReady(player1, new EnslavedScout());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gqs.hasKeyword(gd, scout, Keyword.MOUNTAINWALK)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scout, Keyword.MOUNTAINWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scout, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    void grantedMountainwalkPreventsBlocking() {
        Permanent scout = addCreatureReady(player1, new EnslavedScout());
        harness.addToBattlefield(player2, new Mountain());
        Permanent blocker = addCreatureReady(player2, new EnslavedScout());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        scout.setAttacking(true);

        prepareDeclareBlockers(player1);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scout);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mountainControlledByAttackerDoesNotPreventBlocking() {
        Permanent scout = addCreatureReady(player1, new EnslavedScout());
        harness.addToBattlefield(player1, new Mountain());
        addCreatureReady(player2, new EnslavedScout());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, scout, Keyword.MOUNTAINWALK)).isTrue();
        scout.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0)))).doesNotThrowAnyException();
    }

    @Test
    void grantsMountainwalkOnlyToTheActivatingScout() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new EnslavedScout());
        Permanent otherScout = harness.addToBattlefieldAndReturn(player1, new EnslavedScout());
        Permanent opposingScout = harness.addToBattlefieldAndReturn(player2, new EnslavedScout());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gqs.hasKeyword(gd, scout, Keyword.MOUNTAINWALK)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scout, Keyword.MOUNTAINWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherScout, Keyword.MOUNTAINWALK)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingScout, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    void canActivateWhileTapped() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new EnslavedScout());
        scout.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, scout, Keyword.MOUNTAINWALK)).isTrue();
        assertThat(scout.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new EnslavedScout());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, scout, Keyword.MOUNTAINWALK)).isFalse();
    }
}
