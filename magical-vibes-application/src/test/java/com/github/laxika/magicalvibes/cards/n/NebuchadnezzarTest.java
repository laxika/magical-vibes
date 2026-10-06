package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.Abomination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Nebuchadnezzar.class, Abomination.class})
class NebuchadnezzarTest extends BaseCardTest {

    @Test
    @DisplayName("Name suggestions do not reveal changes to an opponent's hidden hand")
    void nameSuggestionsDoNotDependOnHiddenHand() {
        var permanent = addCreatureReady(player1, new Nebuchadnezzar());
        harness.setHand(player2, List.of(new Abomination()));

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        List<String> firstOptions = List.copyOf(
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options());
        harness.handleListChoice(player1, "Nebuchadnezzar");

        permanent.untap();
        harness.setHand(player2, List.of(new Nebuchadnezzar()));
        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        List<String> secondOptions = List.copyOf(
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options());
        harness.handleListChoice(player1, "Nebuchadnezzar");

        assertThat(secondOptions).containsExactlyElementsOf(firstOptions);
    }

    @Test
    @DisplayName("Can activate during upkeep and pays the tap cost immediately")
    void activatesDuringUpkeepAndTaps() {
        var permanent = addCreatureReady(player1, new Nebuchadnezzar());
        Nebuchadnezzar namedCard = new Nebuchadnezzar();
        harness.setHand(player2, List.of(namedCard));
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, player2.getId());
        assertThat(permanent.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Nebuchadnezzar");

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(namedCard);
    }

    @Test
    @DisplayName("A name absent from the revealed cards discards nothing")
    void nonmatchingNameDiscardsNothing() {
        addCreatureReady(player1, new Nebuchadnezzar());
        Abomination other = new Abomination();
        harness.setHand(player2, List.of(other));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Nebuchadnezzar");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay an X cost greater than available mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new Nebuchadnezzar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick creature cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new Nebuchadnezzar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Chooses a name and discards every matching card among the revealed hand")
    void discardsMatchingRevealedCards() {
        addCreatureReady(player1, new Nebuchadnezzar());
        Nebuchadnezzar firstNamed = new Nebuchadnezzar();
        Nebuchadnezzar secondNamed = new Nebuchadnezzar();
        Abomination other = new Abomination();
        harness.setHand(player2, List.of(firstNamed, secondNamed, other));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 4, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Nebuchadnezzar");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(firstNamed, secondNamed);
    }

    @Test
    @DisplayName("Discards only matching cards in the randomly revealed subset")
    void discardsOnlyMatchingRandomlyRevealedCards() {
        addCreatureReady(player1, new Nebuchadnezzar());
        Nebuchadnezzar firstNamed = new Nebuchadnezzar();
        Nebuchadnezzar secondNamed = new Nebuchadnezzar();
        Abomination other = new Abomination();
        harness.setHand(player2, List.of(firstNamed, secondNamed, other));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Nebuchadnezzar");

        assertThat(gd.playerHands.get(player2.getId())).hasSizeBetween(2, 3).contains(other);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .hasSizeBetween(0, 1)
                .allMatch(card -> card == firstNamed || card == secondNamed);
    }

    @Test
    @DisplayName("X of zero reveals no cards and discards nothing")
    void zeroRevealsNoCards() {
        addCreatureReady(player1, new Nebuchadnezzar());
        Nebuchadnezzar namedCard = new Nebuchadnezzar();
        harness.setHand(player2, List.of(namedCard));

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Nebuchadnezzar");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(namedCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty hand reveals no cards and discards nothing")
    void emptyHandRevealsNoCards() {
        addCreatureReady(player1, new Nebuchadnezzar());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Nebuchadnezzar");

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetController() {
        addCreatureReady(player1, new Nebuchadnezzar());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only during its controller's turn")
    void onlyDuringYourTurn() {
        addCreatureReady(player1, new Nebuchadnezzar());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }
}
