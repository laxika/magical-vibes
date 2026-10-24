package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GaeasTouch;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.n.NecromancersMagemark;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FailOnLegacyDepartureFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WormsOfTheEarth.class, Forest.class, GaeasTouch.class})
class WormsOfTheEarthTest extends BaseCardTest {

    @Test
    @DisplayName("Worms of the Earth prevents land plays")
    void preventsLandPlays() {
        harness.addToBattlefield(player1, new WormsOfTheEarth());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest()));
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The active player receives the first upkeep choice")
    void activePlayerChoosesFirst() {
        harness.addToBattlefield(player1, new WormsOfTheEarth());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Worms of the Earth");
    }

    @Test
    @DisplayName("Lands cannot enter the battlefield from hand")
    void preventsLandsEnteringFromHand() {
        harness.addToBattlefield(player1, new WormsOfTheEarth());
        harness.addToBattlefield(player1, new GaeasTouch());
        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .isEmpty();
    }

    @Test
    @DisplayName("Sacrificing two lands destroys Worms of the Earth")
    void sacrificingTwoLandsDestroysIt() {
        harness.addToBattlefield(player1, new WormsOfTheEarth());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Worms of the Earth");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Choosing the sacrifice option lets the player choose which two lands to sacrifice")
    void choosesTwoLandsWhenMoreAreAvailable() {
        harness.addToBattlefield(player1, new WormsOfTheEarth());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        List<java.util.UUID> forestIds = findPermanents(player1, "Forest").stream()
                .map(permanent -> permanent.getId())
                .toList();
        harness.handleMultiplePermanentsChosen(player1, forestIds.subList(0, 2));

        harness.assertNotOnBattlefield(player1, "Worms of the Earth");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Accepting the damage option deals 5 damage and destroys Worms of the Earth")
    void acceptingDamageDestroysIt() {
        harness.addToBattlefield(player1, new WormsOfTheEarth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 15);
        harness.assertNotOnBattlefield(player1, "Worms of the Earth");
    }

    @Test
    @DisplayName("Declining both upkeep choices leaves Worms of the Earth in play")
    void decliningBothChoicesKeepsIt() {
        harness.addToBattlefield(player1, new WormsOfTheEarth());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Worms of the Earth");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The opponent may accept damage after the active player declines both options")
    void opponentCanChooseAfterActivePlayerDeclinesBothOptions() {
        harness.addToBattlefield(player1, new WormsOfTheEarth());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
        harness.assertNotOnBattlefield(player1, "Worms of the Earth");
        assertThat(findPermanents(player1, "Forest")).hasSize(2);
    }

    @Test
    @DisplayName("A player with two lands may choose damage instead of sacrificing")
    void canChooseDamageWhileControllingTwoLands() {
        harness.addToBattlefield(player1, new WormsOfTheEarth());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 15);
        harness.assertNotOnBattlefield(player1, "Worms of the Earth");
        assertThat(findPermanents(player1, "Forest")).hasSize(2);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, DryadArbor.class, NecromancersMagemark.class})
    @DisplayName("Sacrificing the only two lands asks for a Dryad Arbor's death replacement before either is sacrificed")
    void loneTwoLandsAskForTheDeathReplacementBeforeEitherIsSacrificed() {
        Permanent arbor = setUpEnchantedArborAgainstKalitas();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertDeathReplacementAskedWhileLandsStay(arbor);

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertArborReturnedAndWormsDestroyed(arbor);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, DryadArbor.class, NecromancersMagemark.class})
    @DisplayName("Choosing two lands asks for a Dryad Arbor's death replacement before either is sacrificed")
    void chosenLandsAskForTheDeathReplacementBeforeEitherIsSacrificed() {
        Permanent arbor = setUpEnchantedArborAgainstKalitas();
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1,
                List.of(arbor.getId(), findPermanents(player1, "Forest").getFirst().getId()));

        assertDeathReplacementAskedWhileLandsStay(arbor);

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertArborReturnedAndWormsDestroyed(arbor);
        assertThat(findPermanents(player1, "Forest")).hasSize(1);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, DryadArbor.class, NecromancersMagemark.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Both sacrificed lands' death replacements are asked before either leaves, and they leave together")
    void bothLandsAreAskedBeforeEitherIsSacrificed() {
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        Permanent firstArbor = harness.addToBattlefieldAndReturn(player1, new DryadArbor());
        harness.addToBattlefieldAndReturn(player1, new NecromancersMagemark()).setAttachedTo(firstArbor.getId());
        Permanent secondArbor = harness.addToBattlefieldAndReturn(player1, new DryadArbor());
        harness.addToBattlefieldAndReturn(player1, new NecromancersMagemark()).setAttachedTo(secondArbor.getId());
        harness.addToBattlefield(player1, new WormsOfTheEarth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder(MAGEMARK_RETURN, "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstArbor, secondArbor);
        harness.assertOnBattlefield(player1, "Worms of the Earth");

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertThat(gd.playerHands.get(player1.getId())).contains(firstArbor.getCard(), secondArbor.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstArbor, secondArbor);
        harness.assertNotOnBattlefield(player1, "Worms of the Earth");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private static final String MAGEMARK_RETURN = "Return it to its owner's hand (Necromancer's Magemark)";

    private Permanent setUpEnchantedArborAgainstKalitas() {
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        Permanent arbor = harness.addToBattlefieldAndReturn(player1, new DryadArbor());
        harness.addToBattlefieldAndReturn(player1, new NecromancersMagemark()).setAttachedTo(arbor.getId());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new WormsOfTheEarth());
        return arbor;
    }

    private void assertDeathReplacementAskedWhileLandsStay(Permanent arbor) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder(MAGEMARK_RETURN, "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(arbor);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Worms of the Earth");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices"))
                .isEmpty();
    }

    private void assertArborReturnedAndWormsDestroyed(Permanent arbor) {
        assertThat(gd.playerHands.get(player1.getId())).contains(arbor.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(arbor);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Worms of the Earth");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
