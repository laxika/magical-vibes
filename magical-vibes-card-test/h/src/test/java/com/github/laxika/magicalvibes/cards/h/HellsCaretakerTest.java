package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HellsCaretaker.class, GrizzlyBears.class, LlanowarElves.class, CounselOfTheSoratami.class})
class HellsCaretakerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature during upkeep returns target creature card from graveyard to the battlefield")
    void reanimatesTargetCreatureDuringUpkeep() {
        Permanent caretaker = addCreatureReady(player1, new HellsCaretaker());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());

        Card target = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.handlePermanentChosen(player1, fodder.getId());
        assertThat(caretaker.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(caretaker.isTapped()).isTrue();
        // Sacrificed creature is in the graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");
        // Reanimated creature is on the battlefield, no longer in the graveyard
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
        assertThat(findPermanent(player1, "Llanowar Elves").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate outside the controller's upkeep")
    void cannotActivateOutsideUpkeep() {
        addCreatureReady(player1, new HellsCaretaker());
        addCreatureReady(player1, new GrizzlyBears());

        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotTargetNonCreatureCard() {
        addCreatureReady(player1, new HellsCaretaker());
        addCreatureReady(player1, new GrizzlyBears());

        Card target = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player1);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        addCreatureReady(player1, new HellsCaretaker());
        addCreatureReady(player1, new GrizzlyBears());

        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        advanceToUpkeep(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("allowed graveyard");
    }

    @Test
    @DisplayName("Fizzles if the targeted creature card leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        addCreatureReady(player1, new HellsCaretaker());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());

        Card target = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.handlePermanentChosen(player1, fodder.getId());
        gd.playerGraveyards.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("May sacrifice itself as the creature cost")
    void maySacrificeItselfAsCost() {
        addCreatureReady(player1, new HellsCaretaker());
        Card target = new LlanowarElves();
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player1);

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hell's Caretaker");
        harness.assertNotOnBattlefield(player1, "Hell's Caretaker");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's upkeep")
    void cannotActivateDuringOpponentsUpkeep() {
        addCreatureReady(player1, new HellsCaretaker());
        addCreatureReady(player1, new GrizzlyBears());

        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        advanceToUpkeep(player2);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        addCreatureReady(player1, new HellsCaretaker());
        addCreatureReady(player1, new GrizzlyBears());

        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        advanceToUpkeep(player1);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot return the creature being sacrificed because targets are chosen before costs")
    void cannotTargetCreatureStillOnBattlefield() {
        Permanent caretaker = addCreatureReady(player1, new HellsCaretaker());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of());
        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(fodder.getCard().getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(caretaker.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate without a graveyard target even when a creature can be sacrificed")
    void cannotActivateWithoutTarget() {
        Permanent caretaker = addCreatureReady(player1, new HellsCaretaker());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of());
        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(caretaker.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Hell's Caretaker");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent caretaker = addCreatureReady(player1, new HellsCaretaker());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        advanceToUpkeep(player1);
        caretaker.tap();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Hell's Caretaker");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A Caretaker that enters during upkeep cannot activate without haste")
    void cannotActivateWithSummoningSickness() {
        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new HellsCaretaker());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertOnBattlefield(player1, "Hell's Caretaker");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
