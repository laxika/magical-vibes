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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Seahunter.class, RootwaterCommando.class, BattlefieldPercher.class, Daze.class})
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
        harness.addToBattlefield(player1, new Seahunter());
        Permanent seahunter = findPermanent(player1, "Seahunter");
        seahunter.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        return seahunter;
    }
}
