package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmergencyEject.class, Forest.class, GrizzlyBears.class})
class EmergencyEjectTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonland permanent and gives its controller a Lander token")
    void destroysNonlandPermanentAndCreatesLanderForItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanents(player2, "Lander")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new EmergencyEject()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Lander sacrifices itself to search for a basic land onto the battlefield tapped")
    void landerSearchesForTappedBasicLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target);

        Card land = new Forest();
        harness.setLibrary(player2, List.of(land));

        Permanent lander = findPermanents(player2, "Lander").getFirst();
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(lander), 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).allMatch(card ->
                card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC));

        harness.handleCardChosen(player2, 0);

        harness.assertNotOnBattlefield(player2, "Lander");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(land.getId()) && permanent.isTapped());
    }

    @Test
    void givesLanderToControllerOfOwnTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(target);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Lander")).hasSize(1);
        assertThat(findPermanents(player2, "Lander")).isEmpty();
    }

    @Test
    void createsLanderEvenWhenTargetRegenerates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setRegenerationShield(1);

        cast(target);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(findPermanents(player2, "Lander")).hasSize(1);
    }

    @Test
    void createsNoLanderWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EmergencyEject()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Emergency Eject");
        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanents(player2, "Lander")).isEmpty();
    }

    @Test
    void canFailToFindBasicLandAndStillPaysSacrificeCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(target);
        Card land = new Forest();
        harness.setLibrary(player2, List.of(land));
        Permanent lander = findPermanents(player2, "Lander").getFirst();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(lander), 0, null, null);
        harness.assertNotOnBattlefield(player2, "Lander");
        harness.passBothPriorities();
        harness.handleCardChosen(player2, -1);

        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void cast(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EmergencyEject()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
