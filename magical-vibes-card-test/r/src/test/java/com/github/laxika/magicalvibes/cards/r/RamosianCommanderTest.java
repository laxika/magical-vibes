package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.j.JhovallQueen;
import com.github.laxika.magicalvibes.cards.j.JhovallRider;
import com.github.laxika.magicalvibes.cards.l.LastBreath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RamosianCommander.class, FreshVolunteers.class, RamosianCaptain.class, JhovallRider.class,
        JhovallQueen.class, DeadlyInsect.class, LastBreath.class})
class RamosianCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability pays six generic mana and taps Ramosian Commander")
    void activationPaysManaAndTapsCommander() {
        addReadyCommander();
        harness.setLibrary(player1, List.of(new FreshVolunteers()));

        activateCommander();

        assertThat(findPermanent(player1, "Ramosian Commander").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The activated ability offers Rebel permanents with mana value 5 or less")
    void searchOffersOnlyMatchingRebelPermanents() {
        addReadyCommander();
        harness.setLibrary(player1, List.of(
                new FreshVolunteers(),
                new RamosianCaptain(),
                new JhovallRider(),
                new JhovallQueen(),
                new DeadlyInsect(),
                new LastBreath()));

        activateCommander();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Fresh Volunteers", "Ramosian Captain", "Jhovall Rider");
    }

    @Test
    @DisplayName("The activated ability puts the chosen Rebel permanent onto the battlefield")
    void putsChosenRebelOntoBattlefield() {
        addReadyCommander();
        harness.setLibrary(player1, List.of(new FreshVolunteers()));

        activateCommander();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Commander", "Fresh Volunteers");
    }

    @Test
    @DisplayName("The activated ability does nothing when no matching Rebel is in the library")
    void noMatchingRebelFound() {
        addReadyCommander();
        harness.setLibrary(player1, List.of(new JhovallQueen(), new DeadlyInsect(), new LastBreath()));

        activateCommander();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Commander");
    }

    @Test
    @DisplayName("The search may fail to find even when an eligible Rebel is present")
    void mayDeclineToFindEligibleRebel() {
        addReadyCommander();
        FreshVolunteers rebel = new FreshVolunteers();
        harness.setLibrary(player1, List.of(rebel));

        activateCommander();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(rebel);
        assertThat(countPermanents(player1, "Fresh Volunteers")).isZero();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("A Rebel at the mana value limit enters untapped without paying its mana cost")
    void recruitsManaValueFiveRebel() {
        addReadyCommander();
        JhovallRider rebel = new JhovallRider();
        harness.setLibrary(player1, List.of(rebel, new LastBreath()));

        activateCommander();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Jhovall Rider").isTapped()).isFalse();
        assertThat(findPermanent(player1, "Jhovall Rider").isSummoningSick()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(rebel).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Jhovall Rider");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The ability resolves even after Ramosian Commander leaves the battlefield")
    void abilityResolvesWithoutCommander() {
        addReadyCommander();
        harness.setLibrary(player1, List.of(new FreshVolunteers()));
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player2, List.of(new LastBreath()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, findPermanent(player1, "Ramosian Commander").getId());
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Ramosian Commander");
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Fresh Volunteers");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new RamosianCommander());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(findPermanent(player1, "Ramosian Commander").isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Five mana cannot pay the six-mana activation cost")
    void cannotActivateWithInsufficientMana() {
        addCreatureReady(player1, new RamosianCommander());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(findPermanent(player1, "Ramosian Commander").isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent activation or leave an unresolved search")
    void resolvesWithEmptyLibrary() {
        addReadyCommander();
        harness.setLibrary(player1, List.of());

        activateCommander();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
        assertThat(countPermanents(player1, "Ramosian Commander")).isEqualTo(1);
    }

    private void addReadyCommander() {
        addCreatureReady(player1, new RamosianCommander());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }

    private void activateCommander() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
