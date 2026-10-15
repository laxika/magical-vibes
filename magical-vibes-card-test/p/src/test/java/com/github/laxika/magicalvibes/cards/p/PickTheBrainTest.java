package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.s.StoicBuilder;
import com.github.laxika.magicalvibes.cards.r.ReduceToAshes;
import com.github.laxika.magicalvibes.cards.d.DenyExistence;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PickTheBrain.class, Forest.class, ReduceToAshes.class, DenyExistence.class,
        StoicBuilder.class, MagnifyingGlass.class, CosisTrickster.class})
class PickTheBrainTest extends BaseCardTest {

    @Test
    @DisplayName("Without delirium, exiles one chosen nonland card")
    void withoutDeliriumExilesOneChosenCard() {
        ReduceToAshes first = new ReduceToAshes();
        ReduceToAshes second = new ReduceToAshes();
        ReduceToAshes graveyardCopy = new ReduceToAshes();
        ReduceToAshes libraryCopy = new ReduceToAshes();
        harness.setHand(player2, new ArrayList<>(List.of(first, second, new Forest())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardCopy)));
        harness.setLibrary(player2, new ArrayList<>(List.of(libraryCopy, new Forest())));

        castPickTheBrain();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Reduce to Ashes");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Reduce to Ashes", "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCopy);
        assertThat(gd.playerDecks.get(player2.getId())).contains(libraryCopy);
    }

    @Test
    @DisplayName("With delirium, exiles the chosen card and any number of same-name cards")
    void withDeliriumExilesChosenCardAndAnyNumberOfCopies() {
        ReduceToAshes first = new ReduceToAshes();
        ReduceToAshes second = new ReduceToAshes();
        ReduceToAshes graveyardCopy = new ReduceToAshes();
        ReduceToAshes libraryCopy = new ReduceToAshes();
        harness.setHand(player2, new ArrayList<>(List.of(first, second, new Forest())));
        harness.setGraveyard(player1, new ArrayList<>(List.of(
                new ReduceToAshes(), new DenyExistence(), new StoicBuilder(), new MagnifyingGlass())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(
                graveyardCopy, new DenyExistence(), new StoicBuilder(), new MagnifyingGlass())));
        harness.setLibrary(player2, new ArrayList<>(List.of(libraryCopy, new Forest())));

        castPickTheBrain();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Reduce to Ashes");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Reduce to Ashes", "Reduce to Ashes");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Reduce to Ashes", "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Deny Existence", "Stoic Builder", "Magnifying Glass");
        assertThat(gd.playerDecks.get(player2.getId())).contains(libraryCopy);
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new PickTheBrain()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    void deliriumRevealsWholeHandBeforeChoosing() {
        enableDelirium();
        harness.setHand(player2, List.of(new ReduceToAshes(), new Forest()));
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        castPickTheBrain();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Reduce to Ashes") && message.contains("Forest"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Reduce to Ashes") && message.contains("Forest"));
        harness.handleListChoice(player1, "Reduce to Ashes");
    }

    @Test
    void deliriumRevealsLandOnlyHandAndExilesNothing() {
        enableDelirium();
        Forest land = new Forest();
        harness.setHand(player2, List.of(land));

        castPickTheBrain();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anyMatch(message -> message.contains("Forest"));
    }

    @Test
    void deliriumAllowsExilingNoAdditionalCopies() {
        enableDelirium();
        ReduceToAshes chosen = new ReduceToAshes();
        ReduceToAshes remaining = new ReduceToAshes();
        harness.setHand(player2, List.of(chosen, remaining));

        castPickTheBrain();
        harness.handleListChoice(player1, "Reduce to Ashes");
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void deliriumCanExileCopiesFromEverySearchedZone() {
        enableDelirium();
        ReduceToAshes chosen = new ReduceToAshes();
        ReduceToAshes handCopy = new ReduceToAshes();
        ReduceToAshes graveyardCopy = new ReduceToAshes();
        ReduceToAshes libraryCopy = new ReduceToAshes();
        harness.setHand(player2, List.of(chosen, handCopy, new Forest()));
        harness.setGraveyard(player2, List.of(graveyardCopy));
        harness.setLibrary(player2, List.of(libraryCopy, new Forest()));

        castPickTheBrain();
        harness.handleListChoice(player1, "Reduce to Ashes");
        harness.handleMultipleCardsChosen(player1,
                List.of(handCopy.getId(), graveyardCopy.getId(), libraryCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(chosen, handCopy, graveyardCopy, libraryCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void deliriumShuffleTriggersEvenWhenNoAdditionalCopiesExist() {
        enableDelirium();
        var trickster = harness.addToBattlefieldAndReturn(player1, new CosisTrickster());
        harness.setHand(player2, List.of(new ReduceToAshes()));
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest()));

        castPickTheBrain();
        harness.handleListChoice(player1, "Reduce to Ashes");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void threeCardTypesDoNotEnableDeliriumEvenIfOpponentHasFour() {
        harness.setGraveyard(player1, List.of(new Forest(), new StoicBuilder(), new DenyExistence()));
        harness.setGraveyard(player2, List.of(
                new ReduceToAshes(), new DenyExistence(), new StoicBuilder(), new MagnifyingGlass()));
        ReduceToAshes chosen = new ReduceToAshes();
        ReduceToAshes remaining = new ReduceToAshes();
        harness.setHand(player2, List.of(chosen, remaining));

        castPickTheBrain();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotChooseLandWithoutDelirium() {
        harness.setHand(player2, List.of(new Forest(), new ReduceToAshes()));

        castPickTheBrain();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 1);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName).containsExactly("Reduce to Ashes");
    }

    private void enableDelirium() {
        harness.setGraveyard(player1, List.of(
                new ReduceToAshes(), new DenyExistence(), new StoicBuilder(), new MagnifyingGlass()));
    }

    private void castPickTheBrain() {
        harness.setHand(player1, List.of(new PickTheBrain()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
