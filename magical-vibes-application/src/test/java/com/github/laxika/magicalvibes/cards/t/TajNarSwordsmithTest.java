package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EmpyrialPlate;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.SigilOfDistinction;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TajNarSwordsmith.class, EmpyrialPlate.class, LeoninScimitar.class,
        SigilOfDistinction.class, YotianSoldier.class})
class TajNarSwordsmithTest extends BaseCardTest {

    @Test
    @DisplayName("The enter ability searches for an Equipment within the chosen mana value")
    void searchesForEquipmentWithinChosenManaValue() {
        Card scimitar = new LeoninScimitar();
        Card expensiveEquipment = new EmpyrialPlate();
        harness.setLibrary(player1, List.of(scimitar, expensiveEquipment, new YotianSoldier()));
        castSwordsmithWithMana(5);

        resolveEnterAbility();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 1);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getId).containsExactly(scimitar.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(scimitar.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Choosing zero can find a zero-mana Equipment")
    void zeroCanFindZeroManaEquipment() {
        Card zeroManaEquipment = new SigilOfDistinction();
        harness.setLibrary(player1, List.of(zeroManaEquipment, new YotianSoldier()));
        castSwordsmithWithMana(4);

        resolveEnterAbility();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue()).isZero();
        harness.handleXValueChosen(player1, 0);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getId).containsExactly(zeroManaEquipment.getId());
    }

    @Test
    @DisplayName("A search with no matching Equipment finishes without a card choice")
    void noMatchingEquipmentDoesNotOpenSearch() {
        Card expensiveEquipment = new EmpyrialPlate();
        Card nonEquipment = new YotianSoldier();
        harness.setLibrary(player1, List.of(expensiveEquipment, nonEquipment));
        castSwordsmithWithMana(5);

        resolveEnterAbility();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(expensiveEquipment, nonEquipment);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the optional enter ability does not search or pay")
    void declineDoesNothing() {
        Card equipment = new LeoninScimitar();
        harness.setLibrary(player1, List.of(equipment));
        castSwordsmithWithMana(5);

        resolveEnterAbility();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isOne();
    }

    private void castSwordsmithWithMana(int mana) {
        harness.setHand(player1, List.of(new TajNarSwordsmith()));
        harness.addMana(player1, ManaColor.WHITE, mana);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterAbility() {
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }
}
