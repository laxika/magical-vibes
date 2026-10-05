package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({OminousParcel.class, CrawWurm.class, Forest.class, GrizzlyBears.class, Island.class})
class OminousParcelTest extends BaseCardTest {

    @Test
    @DisplayName("The land ability searches for a basic land and sacrifices Ominous Parcel")
    void searchesForBasicLand() {
        addParcelAndMana(2);
        Card forest = new Forest();
        Card island = new Island();
        setLibrary(forest, island, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest, island);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        harness.assertInGraveyard(player1, "Ominous Parcel");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The damage ability deals 4 damage to a creature and sacrifices Ominous Parcel")
    void dealsDamageToCreature() {
        addParcelAndMana(5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Ominous Parcel");
    }

    @Test
    @DisplayName("The damage ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addParcelAndMana(5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void canFailToFindEvenWithBasicLandAvailable() {
        addParcelAndMana(2);
        Card forest = new Forest();
        setLibrary(forest);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Ominous Parcel");
        harness.assertInGraveyard(player1, "Ominous Parcel");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchWithNoBasicLandStillResolves() {
        addParcelAndMana(2);
        Card creature = new GrizzlyBears();
        setLibrary(creature);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Ominous Parcel");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tappedParcelCannotActivateLandAbility() {
        addParcelAndMana(2);
        findPermanent(player1, "Ominous Parcel").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ominous Parcel");
        harness.assertNotInGraveyard(player1, "Ominous Parcel");
    }

    @Test
    void damageAbilityRequiresFiveMana() {
        addParcelAndMana(4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawWurm());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Ominous Parcel");
        harness.assertNotInGraveyard(player1, "Ominous Parcel");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void canDealLethalDamageToOwnCreatureAfterSacrificingParcel() {
        addParcelAndMana(5);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.assertInGraveyard(player1, "Ominous Parcel");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void revealsTheLandPutIntoHand() {
        addParcelAndMana(2);
        Card forest = new Forest();
        setLibrary(forest);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gameLogContains("reveals Forest and puts it into their hand")).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void searchWithEmptyLibraryStillResolves() {
        addParcelAndMana(2);
        setLibrary();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ominous Parcel");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addParcelAndMana(int mana) {
        harness.addToBattlefield(player1, new OminousParcel());
        harness.addMana(player1, ManaColor.COLORLESS, mana);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
