package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrugaJungle.class, Forest.class, Island.class, Shock.class})
class TrugaJungleTest extends BaseCardTest {

    private void addPlane() {
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TrugaJungle(), gd.nextTimestamp()));
    }

    @Test
    void allLandsGainAnyColorManaAbility() {
        addPlane();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest), null, null);
        harness.handleListChoice(player1, "RED");

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(island), null, null);
        harness.handleListChoice(player2, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void chaosPutsRevealedLandsIntoHandAndRestOnBottom() {
        addPlane();
        Forest forest = new Forest();
        Shock shock = new Shock();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, shock, island));

        PlanechaseService planar = com.github.laxika.magicalvibes.testutil.GameTestEngineContext.get()
                .getBean(PlanechaseService.class);
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, island);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
    }

    @Test
    void chaosLetsControllerOrderNonlandsBelowUnrevealedCards() {
        addPlane();
        Shock first = new Shock();
        Shock second = new Shock();
        Forest forest = new Forest();
        Island unrevealed = new Island();
        harness.setLibrary(player1, List.of(first, forest, second, unrevealed));

        resolveChaos();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(forest).doesNotContain(unrevealed);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unrevealed, second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void chaosAllowsOrderingWhenNoLandsAreRevealed() {
        addPlane();
        Shock first = new Shock();
        Shock second = new Shock();
        Shock third = new Shock();
        harness.setLibrary(player1, List.of(first, second, third));
        var handBefore = List.copyOf(gd.playerHands.get(player1.getId()));

        resolveChaos();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void chaosRevealsAllAvailableCardsInShortLibrary() {
        addPlane();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, island));

        resolveChaos();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, island);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void chaosDoesNothingWithEmptyLibrary() {
        addPlane();
        harness.setLibrary(player1, List.of());
        var handBefore = List.copyOf(gd.playerHands.get(player1.getId()));

        resolveChaos();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void chaosUsesCurrentPlanarControllersLibraryAndHand() {
        addPlane();
        harness.forceActivePlayer(player2);
        gd.planechase.controllerId = player2.getId();
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest));
        harness.setLibrary(player2, List.of(island));

        resolveChaos();

        assertThat(gd.playerHands.get(player2.getId())).contains(island).doesNotContain(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(island, forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void resolveChaos() {
        PlanechaseService planar = com.github.laxika.magicalvibes.testutil.GameTestEngineContext.get()
                .getBean(PlanechaseService.class);
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
    }
}
