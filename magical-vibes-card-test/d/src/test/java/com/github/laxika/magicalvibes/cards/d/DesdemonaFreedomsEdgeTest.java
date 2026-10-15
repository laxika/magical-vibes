package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesdemonaFreedomsEdge.class, Bonesplitter.class, GrizzlyBears.class,
        LavaSpike.class, Ornithopter.class, Shock.class, SolemnSimulacrum.class, ThunderingGiant.class})
class DesdemonaFreedomsEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Attack targets a qualifying creature card in your graveyard")
    void attackTargetsQualifyingCreatureCard() {
        Card artifactCreature = new Ornithopter();
        Card lowManaCreature = new GrizzlyBears();
        Card expensiveCreature = new ThunderingGiant();
        Card noncreatureArtifact = new Bonesplitter();
        harness.setGraveyard(player1, List.of(
                artifactCreature, lowManaCreature, expensiveCreature, noncreatureArtifact));
        addReadyDesdemona();

        declareAttackers(player1, List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                artifactCreature.getId(), lowManaCreature.getId());
    }

    @Test
    @DisplayName("The granted escape permission exiles two other graveyard cards when cast")
    void grantsEscapeAndExilesTwoOtherCards() {
        Card creature = new GrizzlyBears();
        Card firstExiledCard = new Shock();
        Card secondExiledCard = new LavaSpike();
        harness.setGraveyard(player1, List.of(creature, firstExiledCard, secondExiledCard));
        addReadyDesdemona();

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        prepareMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(firstExiledCard.getId(), secondExiledCard.getId());
        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn).doesNotContainKey(creature.getId());
    }

    @Test
    @DisplayName("An artifact creature with mana value greater than three is a legal target")
    void targetsExpensiveArtifactCreatureOnlyInOwnGraveyard() {
        Card creature = new SolemnSimulacrum();
        Card opposingCreature = new SolemnSimulacrum();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        addReadyDesdemona();

        declareAttackers(player1, List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Escape still works after Desdemona leaves the battlefield")
    void escapeDoesNotRequireSourceToRemain() {
        Card creature = new SolemnSimulacrum();
        harness.setGraveyard(player1, List.of(creature,
                new DesdemonaFreedomsEdge(), new DesdemonaFreedomsEdge()));
        grantEscape(creature);
        gd.playerBattlefields.get(player1.getId()).clear();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromGraveyard(player1, 0, List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(creature.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Escape requires two other cards even for a legal target")
    void cannotEscapeWithoutTwoOtherCards() {
        Card creature = new SolemnSimulacrum();
        harness.setGraveyard(player1, List.of(creature, new DesdemonaFreedomsEdge()));
        grantEscape(creature);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The granted escape ability expires at end of turn")
    void escapeExpiresAtEndOfTurn() {
        Card creature = new SolemnSimulacrum();
        harness.setGraveyard(player1, List.of(creature,
                new DesdemonaFreedomsEdge(), new DesdemonaFreedomsEdge()));
        grantEscape(creature);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Escape does not allow a creature without flash to be cast during combat")
    void escapeRespectsCreatureTiming() {
        Card creature = new SolemnSimulacrum();
        harness.setGraveyard(player1, List.of(creature,
                new DesdemonaFreedomsEdge(), new DesdemonaFreedomsEdge()));
        grantEscape(creature);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void grantEscape(Card creature) {
        addReadyDesdemona();
        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addReadyDesdemona() {
        return addCreatureReady(player1, new DesdemonaFreedomsEdge());
    }
}
