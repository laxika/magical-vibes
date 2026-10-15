package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.cards.r.RielleTheEverwise;
import com.github.laxika.magicalvibes.cards.s.SterlingGrove;
import com.github.laxika.magicalvibes.cards.t.TsabosWeb;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Void.class, AncientKavu.class, Forest.class, NomadicElf.class, RielleTheEverwise.class, SterlingGrove.class, TsabosWeb.class})
class VoidTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses a mana value, destroys matching artifacts and creatures, and discards matching nonlands")
    void resolvesChosenManaValue() {
        harness.addToBattlefield(player1, new NomadicElf());
        harness.addToBattlefield(player2, new NomadicElf());
        harness.addToBattlefield(player2, new TsabosWeb());
        harness.addToBattlefield(player2, new AncientKavu());
        harness.addToBattlefield(player2, new SterlingGrove());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player2, List.of(
                new NomadicElf(), new TsabosWeb(), new SterlingGrove(), new AncientKavu(), new Forest()));

        castVoid(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "2");

        harness.assertNotOnBattlefield(player1, "Nomadic Elf");
        harness.assertNotOnBattlefield(player2, "Nomadic Elf");
        harness.assertNotOnBattlefield(player2, "Tsabo's Web");
        harness.assertOnBattlefield(player2, "Ancient Kavu");
        harness.assertOnBattlefield(player2, "Sterling Grove");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Ancient Kavu", "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .contains("Nomadic Elf", "Tsabo's Web", "Sterling Grove");
    }

    @Test
    @DisplayName("Allows choosing zero without discarding cards with another mana value")
    void allowsChoosingZeroWithoutMatchingCards() {
        harness.setHand(player2, List.of(new TsabosWeb(), new Forest()));

        castVoid(player2.getId());

        harness.handleListChoice(player1, "0");

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Tsabo's Web", "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .doesNotContain("Tsabo's Web", "Forest");
    }

    @Test
    @DisplayName("Counts all cards discarded by one resolution as one discard event")
    void countsAllCardsDiscardedInOneEvent() {
        harness.addToBattlefield(player2, new RielleTheEverwise());
        harness.setHand(player2, List.of(new NomadicElf(), new SterlingGrove(), new Forest()));
        harness.setLibrary(player2, List.of(new AncientKavu(), new AncientKavu()));

        castVoid(player2.getId());

        harness.handleListChoice(player1, "2");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Ancient Kavu", "Ancient Kavu");
    }

    @Test
    @DisplayName("Discards matching cards only from the targeted player's hand")
    void discardsOnlyFromTargetPlayersHand() {
        harness.setHand(player1, List.of(new Void(), new NomadicElf()));
        harness.setHand(player2, List.of(new NomadicElf(), new Forest()));
        addVoidMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "2");

        harness.assertInHand(player1, "Nomadic Elf");
        harness.assertNotInHand(player2, "Nomadic Elf");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Rejects a non-player target")
    void rejectsNonPlayerTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NomadicElf());
        harness.setHand(player1, List.of(new Void()));
        addVoidMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Allows choosing a number above all relevant mana values")
    void allowsChoosingArbitraryLargeNumber() {
        harness.addToBattlefield(player2, new NomadicElf());
        harness.setHand(player2, List.of(new AncientKavu(), new Forest()));

        castVoid(player2.getId());
        harness.handleListChoice(player1, "100");

        harness.assertOnBattlefield(player2, "Nomadic Elf");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Ancient Kavu", "Forest");
        assertThat(gameLogContains("reveals their hand")).isTrue();
    }

    @Test
    @DisplayName("Does not reveal hidden hand information through the number choices")
    void numberChoicesDoNotDependOnHiddenHand() {
        harness.setHand(player2, List.of(new NomadicElf()));
        castVoid(player2.getId());
        List<String> firstChoices = gd.interaction
                .activeInteraction(PendingInteraction.ColorChoice.class).options();
        harness.handleListChoice(player1, "0");

        harness.setHand(player2, List.of(new AncientKavu()));
        castVoid(player2.getId());
        List<String> secondChoices = gd.interaction
                .activeInteraction(PendingInteraction.ColorChoice.class).options();

        assertThat(secondChoices).containsExactlyElementsOf(firstChoices);
    }

    @Test
    @DisplayName("Can target its controller and still destroys the opponent's matching permanents")
    void canTargetController() {
        harness.addToBattlefield(player2, new NomadicElf());
        harness.setHand(player1, List.of(new Void(), new NomadicElf(), new Forest()));
        harness.setHand(player2, List.of(new NomadicElf()));
        addVoidMana();

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleListChoice(player1, "2");

        harness.assertNotOnBattlefield(player2, "Nomadic Elf");
        harness.assertNotInHand(player1, "Nomadic Elf");
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player2, "Nomadic Elf");
    }

    @Test
    @DisplayName("Reveals the entire hand only after the number is chosen, including retained cards")
    void revealsHandAfterNumberChoice() {
        harness.setHand(player2, List.of(new NomadicElf(), new AncientKavu(), new Forest()));
        castVoid(player2.getId());

        assertThat(gameLogContains("reveals their hand")).isFalse();
        harness.handleListChoice(player1, "2");

        assertThat(gd.gameLog.stream().map(entry -> entry.plainText())
                .filter(text -> text.contains("reveals their hand")))
                .anySatisfy(text -> assertThat(text)
                        .contains("Nomadic Elf", "Ancient Kavu", "Forest"));
        harness.assertNotInHand(player2, "Nomadic Elf");
        harness.assertInHand(player2, "Ancient Kavu");
        harness.assertInHand(player2, "Forest");
    }

    private void castVoid(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Void()));
        addVoidMana();
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }

    private void addVoidMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
