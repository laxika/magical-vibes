package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.cards.s.SnowHound;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcticFoxes.class, BalduvianBarbarians.class, BalduvianBears.class, Plains.class,
        SnowCoveredPlains.class, SnowHound.class})
class ArcticFoxesTest extends BaseCardTest {

    private Permanent foxAttacking() {
        Permanent fox = addCreatureReady(player1, new ArcticFoxes());
        fox.setAttacking(true);
        return fox;
    }

    private Permanent snowLandOnDefender() {
        return harness.addToBattlefieldAndReturn(player2, new SnowCoveredPlains());
    }

    @Test
    @DisplayName("With snow land, can't be blocked by power 2+")
    void cannotBeBlockedByHighPowerWhenDefenderHasSnowLand() {
        snowLandOnDefender();
        Permanent blocker = addCreatureReady(player2, new BalduvianBarbarians());
        Permanent fox = foxAttacking();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(fox);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With snow land, can be blocked by power less than 2")
    void canBeBlockedByLowPowerWhenDefenderHasSnowLand() {
        snowLandOnDefender();
        Permanent blocker = addCreatureReady(player2, new SnowHound());
        Permanent fox = foxAttacking();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(fox);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("With snow land, can't be blocked by a creature with exactly power 2")
    void cannotBeBlockedByExactlyPowerTwoWhenDefenderHasSnowLand() {
        snowLandOnDefender();
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        Permanent fox = foxAttacking();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(fox);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Without snow land, can be blocked by power 2+")
    void canBeBlockedByHighPowerWithoutSnowLand() {
        Permanent blocker = addCreatureReady(player2, new BalduvianBarbarians());
        Permanent fox = foxAttacking();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(fox);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Non-snow land does not enable the restriction")
    void nonSnowLandDoesNotEnableRestriction() {
        harness.addToBattlefield(player2, new Plains());
        Permanent blocker = addCreatureReady(player2, new BalduvianBarbarians());
        Permanent fox = foxAttacking();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(fox);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A snow nonland does not enable the restriction")
    void snowNonlandDoesNotEnableRestriction() {
        Permanent snowNonland = harness.addToBattlefieldAndReturn(player2, new SnowHound());
        TestCards.mutableCard(snowNonland).setSupertypes(EnumSet.of(CardSupertype.SNOW));
        Permanent blocker = addCreatureReady(player2, new BalduvianBarbarians());
        Permanent fox = foxAttacking();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(fox);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A snow land controlled by the attacker does not enable the restriction")
    void snowLandControlledByAttackerDoesNotEnableRestriction() {
        harness.addToBattlefield(player1, new SnowCoveredPlains());
        Permanent blocker = addCreatureReady(player2, new BalduvianBarbarians());
        Permanent fox = foxAttacking();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(fox);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
