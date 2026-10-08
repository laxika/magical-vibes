package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TorpidMoloch.class, Mountain.class})
class TorpidMolochTest extends BaseCardTest {

    @Test
    void sacrificesThreeLandsAndLosesDefenderUntilEndOfTurn() {
        Permanent moloch = addCreatureReady(player1, new TorpidMoloch());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(moloch.hasKeyword(Keyword.DEFENDER)).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(moloch.hasKeyword(Keyword.DEFENDER)).isTrue();
    }

    @Test
    void cannotActivateWithoutThreeLands() {
        addCreatureReady(player1, new TorpidMoloch());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificesOnlyThreeLandsControlledByActivatingPlayer() {
        Permanent moloch = addCreatureReady(player1, new TorpidMoloch());
        Permanent otherMoloch = addCreatureReady(player1, new TorpidMoloch());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(moloch.hasKeyword(Keyword.DEFENDER)).isFalse();
        assertThat(otherMoloch.hasKeyword(Keyword.DEFENDER)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(moloch, otherMoloch);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void sacrificesLandsAsCostBeforeDefenderIsRemoved() {
        Permanent moloch = addCreatureReady(player1, new TorpidMoloch());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(moloch);
        assertThat(moloch.hasKeyword(Keyword.DEFENDER)).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(moloch.hasKeyword(Keyword.DEFENDER)).isFalse();
    }

    @Test
    void opponentLandsCannotCompleteTheSacrificeCost() {
        Permanent moloch = addCreatureReady(player1, new TorpidMoloch());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(moloch.hasKeyword(Keyword.DEFENDER)).isTrue();
    }

    @Test
    void canActivateWhileTappedAndSummoningSickAndSacrificeTappedLands() {
        Permanent moloch = harness.addToBattlefieldAndReturn(player1, new TorpidMoloch());
        moloch.setSummoningSick(true);
        moloch.tap();
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefieldAndReturn(player1, new Mountain()).tap();
        }

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(moloch.hasKeyword(Keyword.DEFENDER)).isFalse();
        assertThat(moloch.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void choosesExactlyThreeLandsWhenMoreAreAvailable() {
        Permanent moloch = addCreatureReady(player1, new TorpidMoloch());
        Permanent keptLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(moloch, keptLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(moloch.hasKeyword(Keyword.DEFENDER)).isFalse();
    }
}
