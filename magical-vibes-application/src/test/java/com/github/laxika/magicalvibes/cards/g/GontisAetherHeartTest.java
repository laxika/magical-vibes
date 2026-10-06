package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GontisAetherHeart.class, Ornithopter.class, DruidOfTheCowl.class})
class GontisAetherHeartTest extends BaseCardTest {

    @Test
    void getsTwoEnergyWhenItEntersAndWhenAnotherControlledArtifactEnters() {
        castHeart();

        harness.castFromHand(player1, new Ornithopter(), "{0}");
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void doesNotTriggerForNonartifactOrOpponentPermanents() {
        castHeart();

        harness.castFromHand(player1, new DruidOfTheCowl(), "{1}{G}");
        resolveAllTriggers();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new Ornithopter(), "{0}");
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void paysEnergyExilesItselfAndTakesAnExtraTurn() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new GontisAetherHeart());
        gd.playerEnergyCounters.put(player1.getId(), 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(heart);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(heart.getCard());
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void cannotActivateWithoutEightEnergy() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new GontisAetherHeart());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("eight energy counters");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(heart);
    }

    @Test
    void paysCostsImmediatelyAndOnlyGrantsTheTurnOnResolutionDuringOpponentsTurn() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new GontisAetherHeart());
        heart.tap();
        gd.playerEnergyCounters.put(player1.getId(), 10);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(heart);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(heart.getCard());
        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void pendingEnergyTriggerResolvesAfterHeartIsExiledForItsAbility() {
        harness.addToBattlefield(player1, new GontisAetherHeart());
        gd.playerEnergyCounters.put(player1.getId(), 8);
        harness.enterBattlefieldAndReturn(player1, new Ornithopter());
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    void sevenEnergyCannotPayAndFailedActivationLeavesEnergyAndHeartUntouched() {
        Permanent heart = harness.addToBattlefieldAndReturn(player1, new GontisAetherHeart());
        gd.playerEnergyCounters.put(player1.getId(), 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("eight energy counters");

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(7);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(heart);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.extraTurns).isEmpty();
    }

    private void castHeart() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new GontisAetherHeart(), "{6}");
        resolveAllTriggers();
    }
}
