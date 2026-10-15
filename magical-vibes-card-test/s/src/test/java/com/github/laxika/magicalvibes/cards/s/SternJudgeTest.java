package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SternJudge.class, Swamp.class, Mountain.class})
class SternJudgeTest extends BaseCardTest {

    @Test
    @DisplayName("Each player loses life for the Swamps they control")
    void eachPlayerLosesLifeForOwnSwamps() {
        Permanent judge = addCreatureReady(player1, new SternJudge());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Mountain());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
        assertThat(judge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("No player loses life when neither controls a Swamp")
    void doesNothingWhenNoPlayerControlsSwamp() {
        Permanent judge = addCreatureReady(player1, new SternJudge());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Mountain());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(judge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Swamp count is read when the ability resolves")
    void countsSwampsAtResolution() {
        addCreatureReady(player1, new SternJudge());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(swamp);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An opponent's Judge counts tapped Swamps and leaves a Swampless player unaffected")
    void opponentCanActivateAndTappedSwampsCount() {
        addCreatureReady(player2, new SternJudge());
        Permanent swamp = harness.addToBattlefieldAndReturn(player2, new Swamp());
        swamp.tap();
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player1, new Mountain());

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The activated ability resolves after Stern Judge leaves the battlefield")
    void resolvesWithoutItsSource() {
        Permanent judge = addCreatureReady(player1, new SternJudge());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(judge);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Swamps entering after activation are counted at resolution")
    void countsNewSwampsAtResolution() {
        addCreatureReady(player1, new SternJudge());

        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A summoning-sick Judge cannot pay the tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent judge = addCreatureReady(player1, new SternJudge());
        judge.setSummoningSick(true);
        harness.addToBattlefield(player2, new Swamp());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(judge.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A tapped Judge cannot activate again before being untapped")
    void cannotActivateTwiceWhileTapped() {
        addCreatureReady(player1, new SternJudge());
        harness.addToBattlefield(player2, new Swamp());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
