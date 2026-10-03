package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CivicStalwart.class, GrizzlyBears.class})
class CivicStalwartTest extends BaseCardTest {

    private void castStalwart() {
        harness.setHand(player1, List.of(new CivicStalwart()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Entering gives your creatures +1/+1 until end of turn")
    void boostsOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castStalwart();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);

        Permanent stalwart = findPermanent(player1, "Civic Stalwart");
        assertThat(stalwart.getEffectivePower()).isEqualTo(4);
        assertThat(stalwart.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Entering does not boost an opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castStalwart();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castStalwart();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player1, "Civic Stalwart").getEffectivePower()).isEqualTo(3);
        assertThat(findPermanent(player1, "Civic Stalwart").getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A later Stalwart receives only its own boost, while earlier boosts stack")
    void laterCreatureDoesNotReceiveEarlierBoost() {
        castStalwart();
        Permanent first = findPermanent(player1, "Civic Stalwart");

        castStalwart();

        List<Permanent> stalwarts = findPermanents(player1, "Civic Stalwart");
        assertThat(stalwarts).hasSize(2);
        Permanent second = stalwarts.get(1);
        assertThat(first.getEffectivePower()).isEqualTo(5);
        assertThat(first.getEffectiveToughness()).isEqualTo(5);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures entering before the trigger resolves receive the boost")
    void affectedCreaturesAreDeterminedAtResolution() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new CivicStalwart());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new CivicStalwart());

        resolveAllTriggers();

        assertThat(first.getEffectivePower()).isEqualTo(5);
        assertThat(first.getEffectiveToughness()).isEqualTo(5);
        assertThat(second.getEffectivePower()).isEqualTo(5);
        assertThat(second.getEffectiveToughness()).isEqualTo(5);
    }
}
