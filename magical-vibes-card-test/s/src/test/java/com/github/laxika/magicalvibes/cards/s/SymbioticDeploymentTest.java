package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
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

@CardUsed({SymbioticDeployment.class, GaeasSkyfolk.class})
class SymbioticDeploymentTest extends BaseCardTest {

    @Test
    @DisplayName("Controller skips their draw step")
    void controllerSkipsDrawStep() {
        harness.addToBattlefield(player1, new SymbioticDeployment());
        harness.setHand(player1, List.of());
        GaeasSkyfolk topCard = new GaeasSkyfolk();
        harness.setLibrary(player1, List.of(topCard));

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Does not skip an opponent's draw step")
    void doesNotSkipOpponentsDrawStep() {
        harness.addToBattlefield(player1, new SymbioticDeployment());
        harness.setHand(player2, List.of());
        GaeasSkyfolk topCard = new GaeasSkyfolk();
        harness.setLibrary(player2, List.of(topCard));

        gd.turnNumber = 2;
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Tapping two creatures and paying mana draws a card")
    void tapsTwoCreaturesAndDrawsCard() {
        harness.addToBattlefield(player1, new SymbioticDeployment());
        Permanent firstCreature = addCreatureReady(player1, new GaeasSkyfolk());
        Permanent secondCreature = addCreatureReady(player1, new GaeasSkyfolk());
        GaeasSkyfolk drawnCard = new GaeasSkyfolk();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Requires mana in addition to two untapped creatures")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new SymbioticDeployment());
        Permanent firstCreature = addCreatureReady(player1, new GaeasSkyfolk());
        Permanent secondCreature = addCreatureReady(player1, new GaeasSkyfolk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(firstCreature.isTapped()).isFalse();
        assertThat(secondCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without two untapped creatures you control")
    void cannotActivateWithoutTwoUntappedControlledCreatures() {
        harness.addToBattlefield(player1, new SymbioticDeployment());
        addCreatureReady(player1, new GaeasSkyfolk());
        Permanent tappedCreature = addCreatureReady(player1, new GaeasSkyfolk());
        tappedCreature.tap();
        addCreatureReady(player2, new GaeasSkyfolk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Skipping the draw step proceeds directly from upkeep to the main phase")
    void skippedDrawStepHasNoPriorityWindow() {
        harness.addToBattlefield(player1, new SymbioticDeployment());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GaeasSkyfolk()));
        gd.turnNumber = 2;
        advanceToUpkeep(player1);

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Summoning-sick creatures can pay the tap cost before the draw resolves")
    void summoningSickCreaturesCanPayCost() {
        harness.addToBattlefield(player1, new SymbioticDeployment());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GaeasSkyfolk());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GaeasSkyfolk());
        GaeasSkyfolk drawnCard = new GaeasSkyfolk();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
