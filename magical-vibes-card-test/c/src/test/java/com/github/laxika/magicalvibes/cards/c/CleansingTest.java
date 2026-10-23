package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.FontOfAgonies;
import com.github.laxika.magicalvibes.cards.e.EverybodyLives;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.n.NecromancersMagemark;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cleansing.class, CityOfShadows.class, FontOfAgonies.class, EverybodyLives.class})
class CleansingTest extends BaseCardTest {

    @Test
    @DisplayName("Any player can pay 1 life to keep the land")
    void anyPlayerCanPayToKeepLand() {
        harness.addToBattlefield(player2, new CityOfShadows());
        int life2 = gd.getLife(player2.getId());
        castCleansing();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "City of Shadows");
        harness.assertLife(player2, life2 - 1);
    }

    @Test
    @DisplayName("The land is destroyed when no player pays")
    void destroysLandWhenNoPlayerPays() {
        harness.addToBattlefield(player2, new CityOfShadows());
        castCleansing();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "City of Shadows");
        harness.assertInGraveyard(player2, "City of Shadows");
    }

    @Test
    @DisplayName("Each land has an independent payment")
    void paymentsAreIndependentPerLand() {
        harness.addToBattlefield(player2, new CityOfShadows());
        harness.addToBattlefield(player2, new CityOfShadows());
        int life1 = gd.getLife(player1.getId());
        castCleansing();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player2, "City of Shadows")).hasSize(1);
        harness.assertLife(player1, life1 - 1);
    }

    @Test
    @DisplayName("Processes lands controlled by both players")
    void processesLandsControlledByBothPlayers() {
        harness.addToBattlefield(player1, new CityOfShadows());
        harness.addToBattlefield(player2, new CityOfShadows());
        castCleansing();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player1, "City of Shadows");
        harness.assertNotOnBattlefield(player2, "City of Shadows");
        harness.assertInGraveyard(player1, "City of Shadows");
        harness.assertInGraveyard(player2, "City of Shadows");
    }

    @Test
    @DisplayName("A life payment to keep a land triggers Font of Agonies")
    void paymentTriggersLifePaymentAbility() {
        harness.addToBattlefield(player2, new CityOfShadows());
        var font = harness.addToBattlefieldAndReturn(player2, new FontOfAgonies());
        castCleansing();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(font.getCounterCount(CounterType.BLOOD)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does nothing when there are no lands")
    void noLandsNoPrompt() {
        castCleansing();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Unprotected lands are destroyed together after all payment decisions")
    void waitsForAllDecisionsBeforeDestroyingLands() {
        harness.addToBattlefield(player1, new CityOfShadows());
        harness.addToBattlefield(player2, new CityOfShadows());
        castCleansing();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "City of Shadows");
        harness.assertOnBattlefield(player2, "City of Shadows");
        harness.assertNotInGraveyard(player1, "City of Shadows");
        harness.assertNotInGraveyard(player2, "City of Shadows");

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "City of Shadows");
        harness.assertInGraveyard(player2, "City of Shadows");
    }

    @Test
    @DisplayName("Players who cannot lose life cannot pay to protect lands")
    void cannotPayLifeAfterEverybodyLives() {
        harness.addToBattlefield(player2, new CityOfShadows());
        harness.castFromHand(player1, new EverybodyLives(), "{1}{W}");
        harness.passBothPriorities();

        castCleansing();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player2, "City of Shadows");
        harness.assertInGraveyard(player2, "City of Shadows");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A player may pay their exact remaining life to save a land")
    void canPayExactRemainingLife() {
        harness.setLife(player1, 1);
        harness.addToBattlefield(player1, new CityOfShadows());
        castCleansing();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 0);
        harness.assertOnBattlefield(player1, "City of Shadows");
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, DryadArbor.class, NecromancersMagemark.class, PlatinumEmperion.class})
    @DisplayName("Without a prompt, asks for a Dryad Arbor's death replacement before any land is destroyed")
    void withoutPromptAsksForTheDeathReplacementBeforeAnyLandIsDestroyed() {
        Permanent arbor = setUpEnchantedArborAgainstKalitas(player2, player1);
        harness.addToBattlefield(player1, new PlatinumEmperion());
        harness.addToBattlefield(player2, new PlatinumEmperion());
        harness.addToBattlefield(player2, new CityOfShadows());
        castCleansing();

        assertDeathReplacementAskedWhileArborStays(player2, arbor);
        harness.assertOnBattlefield(player2, "City of Shadows");

        harness.handleListChoice(player2, MAGEMARK_RETURN);

        assertArborReturnedToHand(player2, player1, arbor);
        harness.assertInGraveyard(player2, "City of Shadows");
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, DryadArbor.class, NecromancersMagemark.class})
    @DisplayName("Declining every payment asks for a Dryad Arbor's death replacement before it is destroyed")
    void declinedPaymentsAskForTheDeathReplacementBeforeTheArborIsDestroyed() {
        Permanent arbor = setUpEnchantedArborAgainstKalitas(player2, player1);
        castCleansing();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertDeathReplacementAskedWhileArborStays(player2, arbor);

        harness.handleListChoice(player2, MAGEMARK_RETURN);

        assertArborReturnedToHand(player2, player1, arbor);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, DryadArbor.class, NecromancersMagemark.class})
    @DisplayName("After a paid last land, asks for a Dryad Arbor's death replacement without paying again")
    void paidLastLandAsksForTheDeathReplacementWithoutPayingAgain() {
        Permanent arbor = setUpEnchantedArborAgainstKalitas(player1, player2);
        harness.addToBattlefield(player2, new CityOfShadows());
        castCleansing();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player1, true);

        assertDeathReplacementAskedWhileArborStays(player1, arbor);
        harness.assertLife(player1, 19);

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertArborReturnedToHand(player1, player2, arbor);
        harness.assertLife(player1, 19);
        harness.assertOnBattlefield(player2, "City of Shadows");
    }

    private static final String MAGEMARK_RETURN = "Return it to its owner's hand (Necromancer's Magemark)";

    private Permanent setUpEnchantedArborAgainstKalitas(Player arborController, Player kalitasController) {
        harness.addToBattlefield(kalitasController, new KalitasTraitorOfGhet());
        Permanent arbor = harness.addToBattlefieldAndReturn(arborController, new DryadArbor());
        harness.addToBattlefieldAndReturn(arborController, new NecromancersMagemark()).setAttachedTo(arbor.getId());
        return arbor;
    }

    private void assertDeathReplacementAskedWhileArborStays(Player arborController, Permanent arbor) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder(MAGEMARK_RETURN, "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(arborController.getId())).contains(arbor);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("Dryad Arbor is destroyed"))
                .isEmpty();
    }

    private void assertArborReturnedToHand(Player arborController, Player kalitasController, Permanent arbor) {
        assertThat(gd.playerHands.get(arborController.getId())).contains(arbor.getCard());
        assertThat(gd.playerBattlefields.get(arborController.getId())).doesNotContain(arbor);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("Dryad Arbor is destroyed"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(kalitasController.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castCleansing() {
        harness.castFromHand(player1, new Cleansing(), "{W}{W}{W}");
        harness.passBothPriorities();
    }
}
