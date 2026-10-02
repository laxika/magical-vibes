package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.e.ErrantDoomsayers;
import com.github.laxika.magicalvibes.cards.r.RamosianRevivalist;
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

@CardUsed({AmrouScout.class, AmrouSeekers.class, ErrantDoomsayers.class,
        BenalishCavalry.class, AngelsGrace.class, RamosianRevivalist.class})
class AmrouScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Only Rebel permanent cards with mana value 3 or less are offered")
    void searchOffersOnlyMatchingRebelPermanents() {
        addReadyScout();
        AmrouSeekers threeManaRebel = new AmrouSeekers();
        ErrantDoomsayers twoManaRebel = new ErrantDoomsayers();
        harness.setLibrary(player1, List.of(
                threeManaRebel,
                new BenalishCavalry(),
                new AngelsGrace(),
                twoManaRebel));

        activateScout();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(findPermanent(player1, "Amrou Scout").isTapped()).isTrue();
        assertThat(search.params().cards()).containsExactly(threeManaRebel, twoManaRebel);
    }

    @Test
    @DisplayName("The chosen Rebel permanent enters the battlefield")
    void putsChosenRebelOntoBattlefield() {
        addReadyScout();
        AmrouSeekers foundRebel = new AmrouSeekers();
        harness.setLibrary(player1, List.of(foundRebel));

        activateScout();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Amrou Scout", "Amrou Seekers");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == foundRebel);
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("No matching Rebel leaves the library search without a choice")
    void noMatchingRebelFound() {
        addReadyScout();
        harness.setLibrary(player1, List.of(new BenalishCavalry(), new AngelsGrace()));

        activateScout();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Amrou Scout");
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @DisplayName("The ability requires four mana")
    void requiresFourManaToActivate() {
        Permanent scout = addCreatureReady(player1, new AmrouScout());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(scout.isTapped()).isFalse();
    }

    @Test
    void excludesRebelWithManaValueAboveThree() {
        addReadyScout();
        AmrouSeekers eligibleRebel = new AmrouSeekers();
        harness.setLibrary(player1, List.of(new RamosianRevivalist(), eligibleRebel));

        activateScout();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(eligibleRebel);
    }

    @Test
    void mayDeclineToFindAnEligibleRebel() {
        addReadyScout();
        AmrouSeekers eligibleRebel = new AmrouSeekers();
        harness.setLibrary(player1, List.of(eligibleRebel));

        activateScout();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligibleRebel);
        assertThat(countPermanents(player1, "Amrou Seekers")).isZero();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void cannotActivateWhileTapped() {
        addReadyScout();
        findPermanent(player1, "Amrou Scout").setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new AmrouScout());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Amrou Scout").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addReadyScout() {
        addCreatureReady(player1, new AmrouScout());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void activateScout() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
