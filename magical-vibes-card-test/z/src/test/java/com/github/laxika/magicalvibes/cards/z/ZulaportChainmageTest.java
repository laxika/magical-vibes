package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZulaportChainmage.class})
class ZulaportChainmageTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort taps an Ally and makes an opponent lose 2 life")
    void cohortMakesOpponentLoseLife() {
        Permanent chainmage = addCreatureReady(player1, new ZulaportChainmage());
        Permanent ally = addCreatureReady(player1, new ZulaportChainmage());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(chainmage), 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(chainmage.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cohort requires another untapped Ally and an opponent target")
    void cohortRequiresUntappedAllyAndOpponentTarget() {
        Permanent chainmage = addCreatureReady(player1, new ZulaportChainmage());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(chainmage), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(chainmage.isTapped()).isFalse();

        addCreatureReady(player1, new ZulaportChainmage());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(chainmage), 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
        assertThat(chainmage.isTapped()).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    @Test
    @DisplayName("A summoning-sick Ally can pay the additional cohort tap cost")
    void summoningSickAllyCanPayCohortCost() {
        Permanent chainmage = addCreatureReady(player1, new ZulaportChainmage());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new ZulaportChainmage());
        ally.setSummoningSick(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(chainmage), 0, null, player2.getId());

        assertThat(chainmage.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A summoning-sick Chainmage cannot activate cohort")
    void summoningSickSourceCannotActivate() {
        Permanent chainmage = harness.addToBattlefieldAndReturn(player1, new ZulaportChainmage());
        chainmage.setSummoningSick(true);
        Permanent ally = addCreatureReady(player1, new ZulaportChainmage());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(chainmage), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(chainmage.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Ally cannot pay the cohort cost")
    void tappedAllyCannotPayCohortCost() {
        Permanent chainmage = addCreatureReady(player1, new ZulaportChainmage());
        Permanent ally = addCreatureReady(player1, new ZulaportChainmage());
        ally.tap();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(chainmage), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");

        assertThat(chainmage.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Ally cannot pay the cohort cost")
    void opposingAllyCannotPayCohortCost() {
        Permanent chainmage = addCreatureReady(player1, new ZulaportChainmage());
        Permanent ally = addCreatureReady(player2, new ZulaportChainmage());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, battlefieldIndex(chainmage), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");

        assertThat(chainmage.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cohort resolves independently of the tapped creatures")
    void cohortResolvesAfterTappedCreaturesLeave() {
        Permanent chainmage = addCreatureReady(player1, new ZulaportChainmage());
        Permanent ally = addCreatureReady(player1, new ZulaportChainmage());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(chainmage), 0, null, player2.getId());
        assertThat(chainmage.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(chainmage);
        gd.playerBattlefields.get(player1.getId()).remove(ally);
        gd.playerGraveyards.get(player1.getId()).add(chainmage.getCard());
        gd.playerGraveyards.get(player1.getId()).add(ally.getCard());

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }
}
