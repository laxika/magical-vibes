package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CavesOfKoilos;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WayfarersBauble.class, CavesOfKoilos.class, Forest.class, Island.class, Plains.class,
        GrizzlyBears.class})
class WayfarersBaubleTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Wayfarer's Bauble sacrifices it and offers only basic lands tapped")
    void activationOffersBasicLandsTapped() {
        activateBauble();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wayfarer's Bauble");
        harness.assertInGraveyard(player1, "Wayfarer's Bauble");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(3)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Wayfarer's Bauble cannot be activated without two mana")
    void requiresTwoManaToActivate() {
        harness.addToBattlefield(player1, new WayfarersBauble());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Wayfarer's Bauble");
        harness.assertNotInGraveyard(player1, "Wayfarer's Bauble");
    }

    @Test
    @DisplayName("Wayfarer's Bauble cannot be activated while tapped")
    void requiresUntappedSourceToActivate() {
        var bauble = harness.addToBattlefieldAndReturn(player1, new WayfarersBauble());
        bauble.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Wayfarer's Bauble");
        harness.assertNotInGraveyard(player1, "Wayfarer's Bauble");
    }

    @Test
    @DisplayName("Chosen basic land enters the battlefield tapped")
    void chosenLandEntersTapped() {
        activateBauble();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest") && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find a basic land")
    void canFailToFind() {
        activateBauble();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void activateBauble() {
        harness.addToBattlefield(player1, new WayfarersBauble());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(
                new Forest(), new Island(), new Plains(), new CavesOfKoilos(), new GrizzlyBears()));
    }
}
