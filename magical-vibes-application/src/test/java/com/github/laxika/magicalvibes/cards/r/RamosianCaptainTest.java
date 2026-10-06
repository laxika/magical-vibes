package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Brainstorm;
import com.github.laxika.magicalvibes.cards.c.CharmPeddler;
import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        RamosianCaptain.class,
        RamosianSergeant.class,
        RamosianLieutenant.class,
        RamosianCommander.class,
        RamosianSkyMarshal.class,
        DeadlyInsect.class,
        Brainstorm.class,
        CharmPeddler.class
})
class RamosianCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability offers Rebel permanents with mana value 4 or less")
    void searchOffersOnlyMatchingRebelPermanents() {
        addReadyCaptain();
        harness.setLibrary(player1, List.of(
                new RamosianSergeant(),
                new RamosianLieutenant(),
                new RamosianCommander(),
                new RamosianSkyMarshal(),
                new DeadlyInsect(),
                new Brainstorm()));

        activateCaptain();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Ramosian Sergeant", "Ramosian Lieutenant", "Ramosian Commander");
    }

    @Test
    @DisplayName("The activated ability puts the chosen Rebel permanent onto the battlefield")
    void putsChosenRebelOntoBattlefield() {
        addReadyCaptain();
        harness.setLibrary(player1, List.of(new RamosianSergeant()));

        activateCaptain();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Captain", "Ramosian Sergeant");
    }

    @Test
    @DisplayName("The activated ability does nothing when no matching Rebel is in the library")
    void noMatchingRebelFound() {
        addReadyCaptain();
        harness.setLibrary(player1, List.of(new DeadlyInsect(), new Brainstorm()));

        activateCaptain();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Ramosian Captain");
    }

    @Test
    @DisplayName("Activating the ability pays five generic mana and taps Ramosian Captain")
    void activationPaysManaAndTapsCaptain() {
        Permanent captain = addReadyCaptain();
        harness.setLibrary(player1, List.of());

        activateCaptain();

        assertThat(captain.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An inexpensive non-Rebel permanent is not eligible for the search")
    void excludesNonRebelBelowManaValueLimit() {
        addReadyCaptain();
        harness.setLibrary(player1, List.of(new CharmPeddler()));

        activateCaptain();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Charm Peddler");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A Rebel with mana value exactly four enters untapped and summoning sick")
    void recruitsRebelAtManaValueBoundary() {
        addReadyCaptain();
        RamosianCommander commander = new RamosianCommander();
        Brainstorm remainingCard = new Brainstorm();
        harness.setLibrary(player1, List.of(commander, remainingCard));

        activateCaptain();
        harness.handleCardChosen(player1, 0);

        Permanent recruited = findPermanent(player1, "Ramosian Commander");
        assertThat(recruited.isTapped()).isFalse();
        assertThat(recruited.isSummoningSick()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(commander);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The controller may fail to find even when an eligible Rebel is present")
    void mayFailToFindMatchingRebel() {
        addReadyCaptain();
        RamosianSergeant sergeant = new RamosianSergeant();
        harness.setLibrary(player1, List.of(sergeant));

        activateCaptain();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Ramosian Sergeant");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sergeant);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The ability searches only its controller's library")
    void doesNotSearchOpponentsLibrary() {
        addReadyCaptain();
        harness.setLibrary(player1, List.of(new Brainstorm()));
        RamosianCommander opponentCard = new RamosianCommander();
        harness.setLibrary(player2, List.of(opponentCard));

        activateCaptain();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        harness.assertNotOnBattlefield(player1, "Ramosian Commander");
        harness.assertNotOnBattlefield(player2, "Ramosian Commander");
    }

    @Test
    @DisplayName("A summoning-sick Captain cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new RamosianCaptain());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }

    @Test
    @DisplayName("A tapped Captain cannot activate the search ability")
    void cannotActivateWhileTapped() {
        Permanent captain = addReadyCaptain();
        captain.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal combat damage")
    void firstStrikeKillsBlockerBeforeItDealsDamage() {
        Permanent captain = addCreatureReady(player1, new RamosianCaptain());
        addCreatureReady(player2, new RamosianLieutenant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Ramosian Captain");
        harness.assertNotOnBattlefield(player2, "Ramosian Lieutenant");
        assertThat(captain.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The ability cannot be activated with fewer than five mana")
    void cannotActivateWithInsufficientMana() {
        Permanent captain = addCreatureReady(player1, new RamosianCaptain());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(captain.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    private Permanent addReadyCaptain() {
        Permanent captain = addCreatureReady(player1, new RamosianCaptain());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        return captain;
    }

    private void activateCaptain() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
