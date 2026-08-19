package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GontisAetherHeartTest extends BaseCardTest {

    @Test
    void getsTwoEnergyWhenItEntersAndWhenAnotherControlledArtifactEnters() {
        castHeart();

        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
    }

    @Test
    void doesNotTriggerForNonartifactOrOpponentPermanents() {
        castHeart();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Ornithopter()));
        harness.castArtifact(player2, 0);
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

    private void castHeart() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GontisAetherHeart()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
    }
}
