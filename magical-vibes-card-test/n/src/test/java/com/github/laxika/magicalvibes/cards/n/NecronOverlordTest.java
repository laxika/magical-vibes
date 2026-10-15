package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NecronOverlord.class})
class NecronOverlordTest extends BaseCardTest {

    @Test
    void relentlessMarchMakesOpponentLoseXLifeAndTapsArtifacts() {
        Permanent overlord = addCreatureReady(player1, new NecronOverlord());
        Permanent artifact1 = addCreatureReady(player1, new NecronOverlord());
        Permanent artifact2 = addCreatureReady(player1, new NecronOverlord());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(overlord), 2,
                player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(overlord.isTapped()).isTrue();
        assertThat(artifact1.isTapped()).isTrue();
        assertThat(artifact2.isTapped()).isTrue();
    }

    @Test
    void relentlessMarchCannotTargetItsController() {
        Permanent overlord = addCreatureReady(player1, new NecronOverlord());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(overlord), 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
        assertThat(overlord.isTapped()).isFalse();
    }

    @Test
    void zeroXRequiresOnlyTappingTheOverlord() {
        Permanent overlord = addCreatureReady(player1, new NecronOverlord());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, player2.getId());
        assertThat(overlord.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    void sourceCannotAlsoPayTheAdditionalArtifactCost() {
        Permanent overlord = addCreatureReady(player1, new NecronOverlord());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");
        assertThat(overlord.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedAndOpposingArtifactsCannotPayTheCost() {
        Permanent overlord = addCreatureReady(player1, new NecronOverlord());
        Permanent tappedArtifact = addCreatureReady(player1, new NecronOverlord());
        tappedArtifact.tap();
        Permanent opposingArtifact = addCreatureReady(player2, new NecronOverlord());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");
        assertThat(overlord.isTapped()).isFalse();
        assertThat(opposingArtifact.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickArtifactCreatureCanPayTheAdditionalCost() {
        Permanent overlord = addCreatureReady(player1, new NecronOverlord());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new NecronOverlord());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, player2.getId());

        assertThat(overlord.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    void summoningSickOverlordCannotActivateEvenForZeroX() {
        Permanent overlord = harness.addToBattlefieldAndReturn(player1, new NecronOverlord());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(overlord.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void insufficientManaCannotActivateTheAbility() {
        Permanent overlord = addCreatureReady(player1, new NecronOverlord());
        Permanent artifact = addCreatureReady(player1, new NecronOverlord());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(overlord.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
