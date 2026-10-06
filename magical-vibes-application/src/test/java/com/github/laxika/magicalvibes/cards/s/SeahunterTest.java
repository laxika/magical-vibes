package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BattlefieldPercher;
import com.github.laxika.magicalvibes.cards.d.Daze;
import com.github.laxika.magicalvibes.cards.r.RootwaterCommando;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattlefieldPercher.class, Daze.class, RootwaterCommando.class, Seahunter.class})
class SeahunterTest extends BaseCardTest {

    @Test
    @DisplayName("The ability offers only Merfolk permanent cards")
    void searchesForMerfolkPermanent() {
        addReadySeahunter();
        harness.setLibrary(player1, new ArrayList<>(List.of(
                new RootwaterCommando(), new BattlefieldPercher(), new Daze())));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Rootwater Commando");
    }

    @Test
    @DisplayName("The chosen Merfolk permanent enters the battlefield")
    void putsChosenMerfolkOntoBattlefield() {
        Permanent seahunter = addReadySeahunter();
        harness.setLibrary(player1, new ArrayList<>(List.of(new RootwaterCommando())));

        harness.activateAbility(player1, 0, null, null);
        assertThat(seahunter.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Rootwater Commando");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability does nothing when the library has no Merfolk permanent")
    void noMerfolkFound() {
        addReadySeahunter();
        harness.setLibrary(player1, new ArrayList<>(List.of(new BattlefieldPercher(), new Daze())));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Battlefield Percher");
    }

    private Permanent addReadySeahunter() {
        Permanent seahunter = addCreatureReady(player1, new Seahunter());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        return seahunter;
    }

    @Test
    void mayFailToFindEvenWhenMerfolkIsPresent() {
        addReadySeahunter();
        Card merfolk = new RootwaterCommando();
        harness.setLibrary(player1, List.of(merfolk));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(merfolk);
        harness.assertNotOnBattlefield(player1, "Rootwater Commando");
    }

    @Test
    void emptyLibraryCompletesWithoutChoice() {
        addReadySeahunter();
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void insufficientManaDoesNotTapSeahunter() {
        Permanent seahunter = addCreatureReady(player1, new Seahunter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(seahunter.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickSeahunterCannotActivate() {
        Permanent seahunter = harness.addToBattlefieldAndReturn(player1, new Seahunter());
        seahunter.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(seahunter.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedSeahunterCannotActivate() {
        Permanent seahunter = addReadySeahunter();
        seahunter.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void searchesOnlyControllersLibraryAndFindsExactlyOneCard() {
        addReadySeahunter();
        Card chosen = new RootwaterCommando();
        Card remaining = new RootwaterCommando();
        Card opponentsMerfolk = new RootwaterCommando();
        harness.setLibrary(player1, List.of(chosen, remaining));
        harness.setLibrary(player2, List.of(opponentsMerfolk));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsMerfolk);
        assertThat(findPermanent(player1, "Rootwater Commando").isTapped()).isFalse();
        harness.assertNotOnBattlefield(player2, "Rootwater Commando");
    }
}
