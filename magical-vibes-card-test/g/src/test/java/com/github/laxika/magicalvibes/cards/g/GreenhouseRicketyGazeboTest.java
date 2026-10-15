package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreenhouseRicketyGazebo.class, Forest.class, Shock.class})
class GreenhouseRicketyGazeboTest extends BaseCardTest {

    @Test
    void greenhouseGrantsAnyColorManaToLandsYouControl() {
        castRoom(0);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void ricketyGazeboMillsFourAndReturnsUpToTwoPermanents() {
        Card firstPermanent = new Forest();
        Card secondPermanent = new GreenhouseRicketyGazebo();
        Card thirdPermanent = new Forest();
        Card nonPermanent = new Shock();
        harness.setLibrary(player1, List.of(firstPermanent, secondPermanent, thirdPermanent, nonPermanent));

        castRoom(1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2)
                .allMatch(card -> card == firstPermanent
                        || card == secondPermanent
                        || card == thirdPermanent);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(2)
                .contains(nonPermanent);
    }

    @Test
    void greenhouseDoesNotMillWhenItsDoorUnlocks() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        castRoom(0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ricketyGazeboAllowsReturningNoCards() {
        Card first = new Forest();
        Card second = new GreenhouseRicketyGazebo();
        harness.setLibrary(player1, List.of(first, second));

        castRoom(1);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ricketyGazeboCanSkipAnEarlierCardAndReturnOnlyOne() {
        Card skipped = new Forest();
        Card returned = new GreenhouseRicketyGazebo();
        Card alreadyInGraveyard = new Forest();
        harness.setGraveyard(player1, List.of(alreadyInGraveyard));
        harness.setLibrary(player1, List.of(skipped, returned));

        castRoom(1);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(returned);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(alreadyInGraveyard, skipped);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void ricketyGazeboDoesNotOfferNonpermanentsOrOlderGraveyardCards() {
        Card milled = new Shock();
        Card olderPermanent = new Forest();
        harness.setGraveyard(player1, List.of(olderPermanent));
        harness.setLibrary(player1, List.of(milled));

        castRoom(1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(olderPermanent, milled);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void unlockingRicketyGazeboAfterCastingGreenhouseTriggersRecovery() {
        Card milled = new Forest();
        harness.setLibrary(player1, List.of(milled));
        castRoom(0);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.unlockRoomDoor(player1, 0, 1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(milled);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void ricketyGazeboWithAnEmptyLibraryFinishesWithoutAChoice() {
        harness.setLibrary(player1, List.of());

        castRoom(1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void unlockingGreenhouseAfterRicketyGazeboGrantsManaWithoutMillingAgain() {
        harness.setLibrary(player1, List.of());
        castRoom(1);
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.unlockRoomDoor(player1, 0, 0);
        resolveAllTriggers();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new GreenhouseRicketyGazebo()));
        harness.addMana(player1, ManaColor.GREEN, doorIndex == 0 ? 3 : 4);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
