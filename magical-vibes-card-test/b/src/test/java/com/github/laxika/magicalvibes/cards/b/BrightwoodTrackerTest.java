package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.l.LeafkinDruid;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrightwoodTracker.class, LeafkinDruid.class, GreenwoodSentinel.class, Shock.class, Plains.class})
class BrightwoodTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Brightwood Tracker offers creature cards from the top four")
    void activatingOffersCreatureCards() {
        addCreatureReady(player1, new BrightwoodTracker());
        harness.setLibrary(player1, List.of(
                new LeafkinDruid(),
                new Shock(),
                new GreenwoodSentinel(),
                new Plains()
        ));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(eligibleCreatures()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Leafkin Druid", "Greenwood Sentinel");
    }

    @Test
    @DisplayName("Choosing a creature puts it into hand and sends the rest to the bottom")
    void choosingCreaturePutsIntoHand() {
        addCreatureReady(player1, new BrightwoodTracker());
        harness.setLibrary(player1, List.of(new LeafkinDruid(), new Shock(), new GreenwoodSentinel(), new Plains()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        chooseCreature(0);

        harness.assertInHand(player1, "Leafkin Druid");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Shock", "Greenwood Sentinel", "Plains");
    }

    @Test
    @DisplayName("Declining to reveal leaves all four cards for the bottom")
    void mayChooseNoCreature() {
        addCreatureReady(player1, new BrightwoodTracker());
        harness.setLibrary(player1, List.of(new LeafkinDruid(), new Shock(), new GreenwoodSentinel(), new Plains()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        chooseCreature(-1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Leafkin Druid", "Shock", "Greenwood Sentinel", "Plains");
    }

    @Test
    @DisplayName("With no creature cards among the top four, the cards go straight to the bottom")
    void noCreaturesGoesToBottom() {
        addCreatureReady(player1, new BrightwoodTracker());
        harness.setLibrary(player1, List.of(new Shock(), new Plains(), new Shock(), new Plains()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Shock", "Plains", "Shock", "Plains");
    }

    @Test
    @DisplayName("Cannot activate Brightwood Tracker without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new BrightwoodTracker());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate Brightwood Tracker when tapped")
    void cannotActivateWhenTapped() {
        addCreatureReady(player1, new BrightwoodTracker());
        addActivationMana();

        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void onlyTopFourAreEligibleAndUnexaminedCardsStayOnTop() {
        addCreatureReady(player1, new BrightwoodTracker());
        Card fifth = new GreenwoodSentinel();
        Card sixth = new LeafkinDruid();
        Card selected = new LeafkinDruid();
        List<Card> rest = List.of(new Shock(), new Plains(), new Shock());
        harness.setLibrary(player1, List.of(selected, rest.get(0), rest.get(1), rest.get(2), fifth, sixth));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(eligibleCreatures()).containsExactly(selected);
        chooseCreature(0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).contains(selected);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).containsExactly(fifth, sixth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 5)).containsExactlyInAnyOrderElementsOf(rest);
    }

    @Test
    void canChooseFromLibraryWithFewerThanFourCards() {
        addCreatureReady(player1, new BrightwoodTracker());
        Card creature = new LeafkinDruid();
        Card land = new Plains();
        harness.setLibrary(player1, List.of(land, creature));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(eligibleCreatures()).containsExactly(creature);
        chooseCreature(0);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryResolvesWithoutAChoiceOrDraw() {
        addCreatureReady(player1, new BrightwoodTracker());
        harness.setLibrary(player1, List.of());
        int handSize = gd.playerHands.get(player1.getId()).size();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new BrightwoodTracker());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Card> eligibleCreatures() {
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.LibrarySearch search) {
            return search.params().cards();
        }
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        return choice.allCards().stream().filter(card -> choice.validCardIds().contains(card.getId())).toList();
    }

    private void chooseCreature(int index) {
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.LibrarySearch) {
            harness.handleCardChosen(player1, index);
        } else {
            harness.handleMultipleCardsChosen(player1,
                    index < 0 ? List.of() : List.of(eligibleCreatures().get(index).getId()));
        }
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
