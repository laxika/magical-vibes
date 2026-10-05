package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AdventurersAirship;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NoctisPrinceOfLucis.class, Ornithopter.class, GrizzlyBears.class, AdventurersAirship.class})
class NoctisPrinceOfLucisTest extends BaseCardTest {

    @Test
    @DisplayName("casts an artifact from the graveyard by paying 3 life and gives it a finality counter")
    void castsArtifactFromGraveyardForLifeAndFinalityCounter() {
        harness.addToBattlefield(player1, new NoctisPrinceOfLucis());
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent ornithopter = findPermanent(player1, "Ornithopter");
        assertThat(gd.getLife(player1.getId())).isEqualTo(7);
        assertThat(ornithopter.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("cannot cast a nonartifact from the graveyard")
    void cannotCastNonartifactFromGraveyard() {
        harness.addToBattlefield(player1, new NoctisPrinceOfLucis());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast");
    }

    @Test
    @DisplayName("cannot cast an artifact from the graveyard without 3 life")
    void cannotCastArtifactWithoutEnoughLife() {
        harness.addToBattlefield(player1, new NoctisPrinceOfLucis());
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.setLife(player1, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
        assertThat(gd.getLife(player1.getId())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    void paysNormalManaCostInAdditionToLifeForNoncreatureArtifact() {
        harness.addToBattlefield(player1, new NoctisPrinceOfLucis());
        harness.setGraveyard(player1, List.of(new AdventurersAirship()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 7);
        assertThat(findPermanent(player1, "Adventurer's Airship")
                .getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Adventurer's Airship");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void cannotReplaceArtifactManaCostWithLife() {
        harness.addToBattlefield(player1, new NoctisPrinceOfLucis());
        harness.setGraveyard(player1, List.of(new AdventurersAirship()));
        harness.setLife(player1, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Adventurer's Airship");
    }

    @Test
    void artifactCastFromHandDoesNotCostLifeOrGetFinalityCounter() {
        harness.addToBattlefield(player1, new NoctisPrinceOfLucis());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.setLife(player1, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(findPermanent(player1, "Ornithopter")
                .getCounterCount(CounterType.FINALITY)).isZero();
    }

    @Test
    void finalityPersistsWhenNoctisLeavesBeforeResolution() {
        Permanent noctis = harness.addToBattlefieldAndReturn(player1, new NoctisPrinceOfLucis());
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.setLife(player1, 10);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyard(player1, 0);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, noctis));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Noctis, Prince of Lucis");
        harness.assertLife(player1, 7);
        assertThat(findPermanent(player1, "Ornithopter")
                .getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }
}
