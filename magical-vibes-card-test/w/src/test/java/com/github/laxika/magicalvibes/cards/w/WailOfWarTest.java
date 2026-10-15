package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WailOfWar.class, GrizzlyBears.class, LeoninScimitar.class})
class WailOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Debuffs only creatures controlled by the targeted opponent")
    void debuffsTargetOpponentsCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WailOfWar()));
        addMana();

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
        assertThat(opponentCreature.getPowerModifier()).isEqualTo(-1);
        assertThat(opponentCreature.getToughnessModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(opponentCreature.getPowerModifier()).isZero();
        assertThat(opponentCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The debuff mode can target only an opponent")
    void debuffModeRejectsNonOpponent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WailOfWar()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns up to two creature cards from the graveyard to hand")
    void returnsUpToTwoCreatures() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        Card nonCreature = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature, nonCreature));
        harness.setHand(player1, List.of(new WailOfWar()));
        addMana();

        harness.castInstant(player1, 0, 1, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        List<java.util.UUID> targets = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        assertThat(targets).containsExactlyInAnyOrder(firstCreature.getId(), secondCreature.getId());
        harness.handleMultipleCardsChosen(player1, targets);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("The return mode may choose only one of several creature cards")
    void returnsOneSelectedCreature() {
        Card selected = new GrizzlyBears();
        Card unselected = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(selected, unselected));
        harness.setHand(player1, List.of(new WailOfWar()));
        addMana();

        harness.castInstant(player1, 0, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(selected);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unselected).doesNotContain(selected);
    }

    @Test
    @DisplayName("The return mode may choose zero targets even with creatures available")
    void returnsNoCreaturesWhenZeroChosen() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WailOfWar()));
        addMana();

        harness.castInstant(player1, 0, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Wail of War");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The return mode can be cast with an empty graveyard")
    void castsReturnModeWithoutAvailableCreatures() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new WailOfWar()));
        addMana();

        harness.castInstant(player1, 0, 1, null);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MultiGraveyardChoice) {
            harness.handleMultipleCardsChosen(player1, List.of());
        }
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Wail of War");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The return mode rejects more than two targets")
    void rejectsThreeGraveyardTargets() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new WailOfWar()));
        addMana();

        harness.castInstant(player1, 0, 1, null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The return mode cannot target an opponent's graveyard")
    void rejectsOpponentGraveyardTarget() {
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(new WailOfWar()));
        addMana();

        harness.castInstant(player1, 0, 1, null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opposingCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The return mode still returns a legal target when the other leaves the graveyard")
    void returnsRemainingLegalTarget() {
        Card removed = new GrizzlyBears();
        Card remaining = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(removed, remaining));
        harness.setHand(player1, List.of(new WailOfWar()));
        addMana();

        harness.castInstant(player1, 0, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.setExile(player1, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.findExiledCard(removed.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The debuff affects neither noncreatures nor creatures entering afterward")
    void debuffAppliesOnlyToCreaturesPresentAtResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new WailOfWar()));
        addMana();

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        Permanent laterCreature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(creature.getPowerModifier()).isEqualTo(-1);
        assertThat(creature.getToughnessModifier()).isEqualTo(-1);
        assertThat(artifact.getPowerModifier()).isZero();
        assertThat(artifact.getToughnessModifier()).isZero();
        assertThat(laterCreature.getPowerModifier()).isZero();
        assertThat(laterCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Two Wails kill a creature whose toughness becomes zero")
    void debuffsStackAndCauseZeroToughnessDeath() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WailOfWar(), new WailOfWar()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The return mode does not return a target that has left the graveyard")
    void doesNotReturnAnIllegalTarget() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WailOfWar()));
        addMana();

        harness.castInstant(player1, 0, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        harness.assertInGraveyard(player1, "Wail of War");
        assertThat(gd.stack).isEmpty();
    }
}
