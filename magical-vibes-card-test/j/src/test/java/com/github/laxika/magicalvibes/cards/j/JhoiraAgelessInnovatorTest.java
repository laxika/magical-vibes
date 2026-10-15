package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.ChromaticLantern;
import com.github.laxika.magicalvibes.cards.c.CutDown;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoldenArgosy;
import com.github.laxika.magicalvibes.cards.h.HerosHeirloom;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JhoiraAgelessInnovator.class, FountainOfYouth.class, ChromaticLantern.class, GrizzlyBears.class,
        GoldenArgosy.class, HerosHeirloom.class, CutDown.class})
class JhoiraAgelessInnovatorTest extends BaseCardTest {

    @Test
    void addsTwoIngenuityCountersBeforeOfferingAnArtifact() {
        Permanent jhoira = addJhoira();
        FountainOfYouth fountain = new FountainOfYouth();
        harness.setHand(player1, List.of(fountain, new ChromaticLantern(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(jhoira.getCounterCount(CounterType.INGENUITY)).isEqualTo(2);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertInHand(player1, "Chromatic Lantern");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(jhoira.isTapped()).isTrue();
    }

    @Test
    void decliningLeavesEligibleArtifactInHand() {
        Permanent jhoira = addJhoira();
        FountainOfYouth fountain = new FountainOfYouth();
        harness.setHand(player1, List.of(fountain));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(jhoira.getCounterCount(CounterType.INGENUITY)).isEqualTo(2);
        harness.assertInHand(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    void canPutArtifactWithManaValueExactlyEqualToCounterCount() {
        Permanent jhoira = addJhoira();
        harness.setHand(player1, List.of(new HerosHeirloom()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(jhoira.getCounterCount(CounterType.INGENUITY)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Hero's Heirloom");
        harness.assertNotInHand(player1, "Hero's Heirloom");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void successiveActivationsAccumulateCountersAndAllowLargerArtifact() {
        Permanent jhoira = addJhoira();
        harness.setHand(player1, List.of(new GoldenArgosy()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(jhoira.getCounterCount(CounterType.INGENUITY)).isEqualTo(2);
        harness.assertInHand(player1, "Golden Argosy");
        harness.assertNotOnBattlefield(player1, "Golden Argosy");

        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(jhoira.getCounterCount(CounterType.INGENUITY)).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Golden Argosy");
        harness.assertNotInHand(player1, "Golden Argosy");
    }

    @Test
    void stillAddsCountersWhenHandIsEmpty() {
        Permanent jhoira = addJhoira();
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(jhoira.getCounterCount(CounterType.INGENUITY)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removedJhoiraUsesExistingCountersWithoutAddingTwoMore() {
        Permanent jhoira = addJhoira();
        jhoira.getCounters().put(CounterType.INGENUITY, 2);
        harness.setHand(player1, List.of(new HerosHeirloom(), new GoldenArgosy()));
        harness.setHand(player2, List.of(new CutDown()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castInstant(player2, 0, jhoira.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Jhoira, Ageless Innovator");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Hero's Heirloom");
        harness.assertInHand(player1, "Golden Argosy");
        harness.assertNotOnBattlefield(player1, "Golden Argosy");
    }

    private Permanent addJhoira() {
        Permanent jhoira = harness.addToBattlefieldAndReturn(player1, new JhoiraAgelessInnovator());
        jhoira.setSummoningSick(false);
        return jhoira;
    }
}
