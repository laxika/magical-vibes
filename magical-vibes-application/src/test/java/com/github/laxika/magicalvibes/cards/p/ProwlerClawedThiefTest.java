package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DocOcksHenchmen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProwlerClawedThief.class, DocOcksHenchmen.class, GrizzlyBears.class, Mountain.class, RayOfCommand.class})
class ProwlerClawedThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Connives when another Villain you control enters")
    void connivesWhenAnotherVillainEnters() {
        Permanent prowler = addCreatureReady(player1, new ProwlerClawedThief());
        harness.setHand(player1, List.of(new DocOcksHenchmen(), new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    @DisplayName("Does not trigger for a non-Villain creature")
    void doesNotTriggerForNonVillain() {
        Permanent prowler = addCreatureReady(player1, new ProwlerClawedThief());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Does not put a counter on a land discarded during connive")
    void doesNotPutCounterOnLandDiscard() {
        Permanent prowler = addCreatureReady(player1, new ProwlerClawedThief());
        harness.setHand(player1, List.of(new DocOcksHenchmen(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Does not connive when Prowler itself enters")
    void doesNotTriggerForItself() {
        harness.setHand(player1, List.of(new ProwlerClawedThief()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not connive when an opponent's Villain enters")
    void doesNotTriggerForOpposingVillain() {
        Permanent prowler = addCreatureReady(player1, new ProwlerClawedThief());
        harness.setHand(player2, List.of(new DocOcksHenchmen()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Menace requires at least two blockers")
    void menaceRequiresTwoBlockers() {
        addCreatureReady(player1, new ProwlerClawedThief());
        addCreatureReady(player2, new DocOcksHenchmen());
        addCreatureReady(player2, new DocOcksHenchmen());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
    }

    @Test
    @DisplayName("Prowler's current controller draws and discards when it connives")
    void newControllerConnivesAfterControlChanges() {
        Permanent prowler = addCreatureReady(player1, new ProwlerClawedThief());
        Mountain originalHandCard = new Mountain();
        DocOcksHenchmen drawnCard = new DocOcksHenchmen();
        harness.setHand(player1, List.of(new DocOcksHenchmen(), originalHandCard));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setHand(player2, List.of(new RayOfCommand(), new Mountain()));
        harness.setLibrary(player2, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, prowler.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(prowler);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(originalHandCard);
        assertThat(gd.playerHands.get(player2.getId())).contains(drawnCard);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(drawnCard);
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
