package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BeholdTheMultiverse;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
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

@CardUsed({FrostAugur.class, SnowCoveredIsland.class, BeholdTheMultiverse.class})
class FrostAugurTest extends BaseCardTest {

    @Test
    @DisplayName("Offers a snow top card for optional reveal and puts it into hand when accepted")
    void acceptsTopSnowCard() {
        addReadyAugur();
        Card topCard = new SnowCoveredIsland();
        harness.setLibrary(player1, List.of(topCard, new BeholdTheMultiverse()));
        addSnowMana();

        activateAbility();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("A non-snow top card stays on top without offering a choice")
    void nonSnowTopCardStaysOnTop() {
        addReadyAugur();
        Card topCard = new BeholdTheMultiverse();
        harness.setLibrary(player1, List.of(topCard, new SnowCoveredIsland()));
        addSnowMana();

        activateAbility();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Snow mana is required to activate Frost Augur")
    void requiresSnowMana() {
        addReadyAugur();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Declining a snow card leaves the library unchanged")
    void decliningSnowCardLeavesItOnTop() {
        addReadyAugur();
        Card topCard = new SnowCoveredIsland();
        Card nextCard = new BeholdTheMultiverse();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        addSnowMana();

        activateAbility();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library causes no choice or card movement")
    void emptyLibraryDoesNothing() {
        addReadyAugur();
        harness.setLibrary(player1, List.of());
        addSnowMana();
        int handSize = gd.playerHands.get(player1.getId()).size();

        activateAbility();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A snow creature card can be revealed to hand")
    void acceptsSnowCreature() {
        addReadyAugur();
        Card topCard = new FrostAugur();
        harness.setLibrary(player1, List.of(topCard));
        addSnowMana();

        activateAbility();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Activation pays exactly one snow mana and taps the Augur")
    void paysSnowManaAndTaps() {
        addReadyAugur();
        harness.setLibrary(player1, List.of(new BeholdTheMultiverse()));
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(pool.getSnowManaTotal()).isZero();
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The controller sees a non-snow top card privately")
    void privatelyShowsNonSnowTopCard() {
        addReadyAugur();
        Card topCard = new BeholdTheMultiverse();
        harness.setLibrary(player1, List.of(topCard));
        addSnowMana();
        harness.clearMessages();

        activateAbility();

        assertThat(harness.getConn1().getMessagesContaining(topCard.getName())).isNotEmpty();
        assertThat(harness.getConn2().getMessagesContaining(topCard.getName())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    private void addReadyAugur() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent augur = harness.addToBattlefieldAndReturn(player1, new FrostAugur());
        augur.setSummoningSick(false);
    }

    private void addSnowMana() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(ManaColor.BLUE, 1);
    }

    private void activateAbility() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
