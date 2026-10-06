package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.h.HeartPiercerBow;
import com.github.laxika.magicalvibes.cards.o.Octoprophet;
import com.github.laxika.magicalvibes.cards.v.VialOfDragonfire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RenownedWeaponsmith.class, CopperMyr.class, LlanowarElves.class,
        HeartPiercerBow.class, VialOfDragonfire.class, Octoprophet.class})
class RenownedWeaponsmithTest extends BaseCardTest {

    private void setUpWeaponsmith() {
        harness.addToBattlefieldAndReturn(player1, new RenownedWeaponsmith()).setSummoningSick(false);
    }

    @Test
    @DisplayName("Tapping Renowned Weaponsmith adds two artifact-restricted colorless mana")
    void addsArtifactRestrictedMana() {
        setUpWeaponsmith();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Renowned Weaponsmith").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Artifact-restricted mana can pay for an artifact spell")
    void restrictedManaCanPayForArtifactSpell() {
        setUpWeaponsmith();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new CopperMyr()));

        harness.activateAbility(player1, 0, null, null);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
    }

    @Test
    @DisplayName("Artifact-restricted mana cannot pay for a nonartifact spell")
    void restrictedManaCannotPayForNonartifactSpell() {
        setUpWeaponsmith();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LlanowarElves()));

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Second ability offers only Heart-Piercer Bow and Vial of Dragonfire")
    void searchOffersOnlyNamedCards() {
        setUpWeaponsmith();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLibrary(player1, List.of(
                new HeartPiercerBow(), new VialOfDragonfire(), new CopperMyr()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Heart-Piercer Bow", "Vial of Dragonfire");
    }

    @Test
    @DisplayName("Second ability puts the chosen card into hand")
    void chosenCardGoesToHand() {
        setUpWeaponsmith();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLibrary(player1, List.of(new VialOfDragonfire()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Vial of Dragonfire");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void restrictedManaCannotPayGenericCostsOfNonartifactSpells() {
        setUpWeaponsmith();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Octoprophet()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void restrictedManaPaysForArtifactActivation() {
        setUpWeaponsmith();
        harness.addToBattlefield(player1, new VialOfDragonfire());
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, null,
                harness.getPermanentId(player1, "Renowned Weaponsmith"));
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
        harness.assertInGraveyard(player1, "Vial of Dragonfire");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Renowned Weaponsmith");
    }

    @Test
    void searchMayFailToFindEvenWithMatchingCards() {
        setUpWeaponsmith();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new HeartPiercerBow(), new VialOfDragonfire()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void searchWithNoMatchingCardsCompletesWithoutChoice() {
        setUpWeaponsmith();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Octoprophet()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void summoningSicknessPreventsBothTapAbilities() {
        harness.addToBattlefield(player1, new RenownedWeaponsmith());
        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
