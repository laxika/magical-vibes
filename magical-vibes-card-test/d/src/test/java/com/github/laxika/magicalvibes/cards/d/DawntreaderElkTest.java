package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.v.VaultOfTheArchangel;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DawntreaderElk.class, Forest.class, GrizzlyBears.class, Island.class, Plains.class})
class DawntreaderElkTest extends BaseCardTest {

    @Test
    @DisplayName("Dawntreader Elk is sacrificed as part of the cost")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.GREEN, 1);
        setupLibrary(player1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Dawntreader Elk");
    }

    @Test
    @DisplayName("Activating the ability searches for a basic land that enters tapped")
    void searchPutsBasicLandTappedOntoBattlefield() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.GREEN, 1);
        setupLibrary(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve activated ability → library search

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    @Test
    @DisplayName("With no basic lands in library, no land enters the battlefield")
    void failToFindNoBasicLand() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve activated ability → no basic land to find

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Cannot activate the ability without green mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        setupLibrary(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May fail to find even when a basic land is available")
    void mayFailToFindAvailableBasicLand() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    @CardUsed({VaultOfTheArchangel.class})
    @DisplayName("A nonbasic land cannot be found")
    void cannotFindNonbasicLand() {
        harness.addToBattlefield(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(new VaultOfTheArchangel()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vault of the Archangel");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Elk can activate and its ability resolves after sacrifice")
    void tappedSummoningSickElkCanActivate() {
        var elk = harness.addToBattlefieldAndReturn(player1, new DawntreaderElk());
        elk.tap();
        elk.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Dawntreader Elk");
        harness.assertNotOnBattlefield(player1, "Dawntreader Elk");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void setupLibrary(com.github.laxika.magicalvibes.model.Player player) {
        harness.setLibrary(player, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
