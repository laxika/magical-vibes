package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MuscleBurst;
import com.github.laxika.magicalvibes.cards.y.YixlidJailer;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiligentFarmhand.class, Forest.class, Island.class, MuscleBurst.class, YixlidJailer.class})
class DiligentFarmhandTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and searches for a basic land onto the battlefield tapped")
    void sacrificesAndOffersBasicLands() {
        activateAbility();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Diligent Farmhand");
        harness.assertInGraveyard(player1, "Diligent Farmhand");

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(card -> card.getName().equals("Forest") || card.getName().equals("Island"));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("The searched basic land enters tapped")
    void searchedLandEntersTapped() {
        activateAbility();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest")
                        && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The basic land search may fail to find")
    void searchMayFailToFind() {
        harness.addToBattlefield(player1, new DiligentFarmhand());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setLibrary(player1, List.of(new Forest(), new MuscleBurst()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Forest")
                        || permanent.getCard().getName().equals("Island"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Diligent Farmhand in a graveyard counts as a Muscle Burst card")
    void graveyardFarmhandCountsAsMuscleBurst() {
        harness.addToBattlefield(player1, new DiligentFarmhand());
        var target = harness.addToBattlefieldAndReturn(player1, new DiligentFarmhand());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.setHand(player1, List.of(new MuscleBurst()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Muscle Burst counts Farmhands and Muscle Bursts in both graveyards")
    void countsFarmhandsInBothGraveyards() {
        harness.setGraveyard(player1, List.of(new DiligentFarmhand(), new MuscleBurst()));
        harness.setGraveyard(player2, List.of(new DiligentFarmhand(), new DiligentFarmhand(), new MuscleBurst()));
        var target = harness.addToBattlefieldAndReturn(player1, new DiligentFarmhand());
        harness.setHand(player1, List.of(new MuscleBurst(), new DiligentFarmhand()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(8);
        assertThat(target.getToughnessModifier()).isEqualTo(8);
    }

    @Test
    @DisplayName("Yixlid Jailer stops Farmhand counting as Muscle Burst but not actual Muscle Bursts")
    void graveyardAbilityLossStopsCountingAsMuscleBurst() {
        harness.setGraveyard(player1, List.of(new DiligentFarmhand(), new MuscleBurst()));
        harness.setGraveyard(player2, List.of(new DiligentFarmhand()));
        harness.addToBattlefield(player2, new YixlidJailer());
        var target = harness.addToBattlefieldAndReturn(player1, new DiligentFarmhand());
        harness.setHand(player1, List.of(new MuscleBurst()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    private void activateAbility() {
        harness.addToBattlefield(player1, new DiligentFarmhand());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setLibrary(player1, List.of(new Forest(), new Island(), new MuscleBurst()));

        harness.activateAbility(player1, 0, null, null);
    }
}
