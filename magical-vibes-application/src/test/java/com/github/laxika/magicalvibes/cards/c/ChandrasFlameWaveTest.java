package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({ChandrasFlameWave.class, ChandraFlamesFury.class, GiantSpider.class, HillGiant.class})
class ChandrasFlameWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to the target player and each creature they control")
    void damagesTargetPlayerAndTheirCreatures() {
        harness.setLife(player2, 20);
        Permanent targetSpider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent targetGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new ChandrasFlameWave()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(targetSpider.getMarkedDamage()).isEqualTo(2);
        assertThat(targetGiant.getMarkedDamage()).isEqualTo(2);
        assertThat(ownGiant.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Finds Chandra, Flame's Fury in the graveyard and puts it into hand")
    void findsNamedCardInGraveyard() {
        ChandraFlamesFury chandraFlamesFury = new ChandraFlamesFury();
        harness.setGraveyard(player1, List.of(chandraFlamesFury));
        harness.setHand(player1, List.of(new ChandrasFlameWave()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chandraFlamesFury.getId()));

        harness.assertInHand(player1, "Chandra, Flame's Fury");
        harness.assertNotInGraveyard(player1, "Chandra, Flame's Fury");
    }

    @Test
    @DisplayName("Finds Chandra, Flame's Fury in the library and puts it into hand")
    void findsNamedCardInLibrary() {
        ChandraFlamesFury libraryCopy = new ChandraFlamesFury();
        harness.setLibrary(player1, List.of(libraryCopy));
        harness.setHand(player1, List.of(new ChandrasFlameWave()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(libraryCopy.getId()));

        harness.assertInHand(player1, "Chandra, Flame's Fury");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ChandrasFlameWave()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target its controller and damage only that player's creatures")
    void canTargetController() {
        harness.setLife(player1, 20);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ChandrasFlameWave()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can fail to find Chandra in the library without undoing damage")
    void canFailToFindInLibrary() {
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new ChandraFlamesFury()));
        harness.setHand(player1, List.of(new ChandrasFlameWave()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertLife(player2, 18);
        harness.assertNotInHand(player1, "Chandra, Flame's Fury");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A graveyard copy does not force retrieval before choosing which zone to search")
    void offersSearchChoiceWhenBothZonesContainChandra() {
        ChandraFlamesFury graveyardCopy = new ChandraFlamesFury();
        harness.setGraveyard(player1, List.of(graveyardCopy));
        harness.setLibrary(player1, List.of(new ChandraFlamesFury()));
        harness.setHand(player1, List.of(new ChandrasFlameWave()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCopy);
        harness.assertNotInHand(player1, "Chandra, Flame's Fury");
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }
}
