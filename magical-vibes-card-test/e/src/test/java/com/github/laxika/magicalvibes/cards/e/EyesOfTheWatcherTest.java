package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AbunasChant;
import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.c.ChannelTheSuns;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyesOfTheWatcher.class, AbunasChant.class, Arachnoid.class, ChannelTheSuns.class})
class EyesOfTheWatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant and paying {1} opens a scry 2 interaction")
    void instantCastAndPaymentScryTwo() {
        setUp(new Arachnoid(), new ChannelTheSuns());
        harness.setHand(player1, List.of(new AbunasChant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        Card originalTop = gd.playerDecks.get(player1.getId()).get(0);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isNotSameAs(originalTop);
    }

    @Test
    @DisplayName("Declining the payment does not scry")
    void decliningPaymentDoesNotScry() {
        setUp(new Arachnoid(), new ChannelTheSuns());
        harness.setHand(player1, List.of(new AbunasChant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Accepting without enough mana does not scry")
    void acceptingWithoutManaDoesNotScry() {
        setUp(new Arachnoid(), new ChannelTheSuns());
        harness.setHand(player1, List.of(new AbunasChant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Casting a creature does not trigger Eyes of the Watcher")
    void creatureCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new EyesOfTheWatcher());
        harness.setHand(player1, List.of(new Arachnoid()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Eyes of the Watcher"));
    }

    @Test
    @DisplayName("Casting a sorcery also triggers Eyes of the Watcher")
    void sorceryCastTriggers() {
        setUp(new Arachnoid(), new AbunasChant());
        harness.setHand(player1, List.of(new ChannelTheSuns()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger Eyes of the Watcher")
    void opponentInstantDoesNotTrigger() {
        harness.addToBattlefield(player1, new EyesOfTheWatcher());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AbunasChant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of());

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Eyes of the Watcher"));
    }

    @Test
    @DisplayName("Scry can put both cards on the bottom in either order")
    void scryBothCardsToBottom() {
        Card first = new Arachnoid();
        Card second = new AbunasChant();
        Card third = new ChannelTheSuns();
        setUp(first, second, third);
        castSorceryAndPayForScry();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
    }

    @Test
    @DisplayName("Scry can keep one card on top and put the other on the bottom")
    void scrySplitsCardsBetweenTopAndBottom() {
        Card first = new Arachnoid();
        Card second = new AbunasChant();
        Card third = new ChannelTheSuns();
        setUp(first, second, third);
        castSorceryAndPayForScry();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
    }

    @Test
    @DisplayName("Two copies trigger separately and each requires its own payment")
    void twoCopiesRequireSeparatePayments() {
        setUp(new Arachnoid(), new AbunasChant());
        harness.addToBattlefield(player1, new EyesOfTheWatcher());
        castSorceryAndPayForScry();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Scry 2 with one card in the library only looks at that card")
    void scryWithOneCardInLibrary() {
        Card onlyCard = new Arachnoid();
        setUp(onlyCard);
        castSorceryAndPayForScry();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    @DisplayName("Paying to scry an empty library does not require a card ordering choice")
    void scryWithEmptyLibrary() {
        setUp();
        castSorceryAndPayForScry();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castSorceryAndPayForScry() {
        harness.setHand(player1, List.of(new ChannelTheSuns()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void setUp(Card... libraryCards) {
        harness.addToBattlefield(player1, new EyesOfTheWatcher());
        harness.setLibrary(player1, List.of(libraryCards));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
