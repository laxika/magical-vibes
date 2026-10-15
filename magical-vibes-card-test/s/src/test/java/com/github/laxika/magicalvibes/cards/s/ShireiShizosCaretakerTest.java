package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.cards.g.GoryosVengeance;
import com.github.laxika.magicalvibes.cards.h.HerosDemise;
import com.github.laxika.magicalvibes.cards.t.ThreadsOfDisloyalty;
import com.github.laxika.magicalvibes.cards.t.ToshiroUmezawa;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShireiShizosCaretaker.class, FirstVolley.class, Frostling.class,
        GoryosVengeance.class, HerosDemise.class, SickeningShoal.class,
        ThreadsOfDisloyalty.class, ToshiroUmezawa.class})
class ShireiShizosCaretakerTest extends BaseCardTest {

    /** Casts First Volley at a creature and resolves the spell, leaving any Shirei trigger pending. */
    private void firstVolleyAt(Player caster, Player victimController, String victimName) {
        prepareMainPhase(caster);

        harness.setHand(caster, List.of(new FirstVolley()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castInstant(caster, 0, harness.getPermanentId(victimController, victimName));
        harness.passBothPriorities();
    }

    /** Casts Hero's Demise at a legendary creature and resolves the spell. */
    private void herosDemiseAt(Player caster, Player victimController, String victimName) {
        prepareMainPhase(caster);

        harness.setHand(caster, List.of(new HerosDemise()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castInstant(caster, 0, harness.getPermanentId(victimController, victimName));
        harness.passBothPriorities();
    }

    /** Casts Sickening Shoal for X=2 and resolves it, leaving any Shirei trigger pending. */
    private void sickeningShoalForTwoAt(String victimName) {
        prepareMainPhase(player1);

        harness.setHand(player1, List.of(new SickeningShoal()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, 2, harness.getPermanentId(player1, victimName));
        harness.passBothPriorities();
    }

    /** Steals a creature with Threads of Disloyalty so its owner and controller differ. */
    private void threadsSteals(Permanent victim) {
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new ThreadsOfDisloyalty()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player2, 0, victim.getId());
        harness.passBothPriorities();
    }

    private void resolveDeathTrigger() {
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    /** Advances from the precombat main phase to the end step, firing the delayed return. */
    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }
    }

    @Test
    @DisplayName("A power-1 creature that died returns to the battlefield at the next end step")
    void returnsPowerOneCreatureAtEndStep() {
        harness.addToBattlefield(player1, new ShireiShizosCaretaker());
        harness.addToBattlefield(player1, new Frostling());

        firstVolleyAt(player1, player1, "Frostling");
        resolveDeathTrigger();

        harness.assertInGraveyard(player1, "Frostling");

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Frostling");
        harness.assertNotInGraveyard(player1, "Frostling");
    }

    @Test
    @DisplayName("Declining the may leaves the dead creature in the graveyard")
    void declinedReturnLeavesCreatureInGraveyard() {
        harness.addToBattlefield(player1, new ShireiShizosCaretaker());
        harness.addToBattlefield(player1, new Frostling());

        firstVolleyAt(player1, player1, "Frostling");
        resolveDeathTrigger();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Frostling");
        harness.assertInGraveyard(player1, "Frostling");
    }

    @Test
    @DisplayName("A creature with power greater than 1 does not trigger Shirei")
    void doesNotTriggerForPowerTwoCreature() {
        harness.addToBattlefield(player1, new ShireiShizosCaretaker());
        harness.addToBattlefield(player1, new ToshiroUmezawa());

        herosDemiseAt(player1, player1, "Toshiro Umezawa");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Toshiro Umezawa");
        harness.assertInGraveyard(player1, "Toshiro Umezawa");
    }

    @Test
    @DisplayName("Nothing returns if Shirei has left the battlefield by the end step")
    void noReturnWhenShireiLeftTheBattlefield() {
        harness.addToBattlefield(player1, new ShireiShizosCaretaker());
        harness.addToBattlefield(player1, new Frostling());

        firstVolleyAt(player1, player1, "Frostling");
        resolveDeathTrigger();

        herosDemiseAt(player1, player1, "Shirei, Shizo's Caretaker");

        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Frostling");
        harness.assertInGraveyard(player1, "Frostling");
    }

    @Test
    @DisplayName("Uses a creature's power immediately before it dies")
    void returnsCreatureThatDiedWithPowerOneOrLess() {
        harness.addToBattlefield(player1, new ShireiShizosCaretaker());
        harness.addToBattlefield(player1, new ToshiroUmezawa());

        sickeningShoalForTwoAt("Toshiro Umezawa");
        resolveDeathTrigger();

        harness.assertInGraveyard(player1, "Toshiro Umezawa");

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Toshiro Umezawa");
        harness.assertNotInGraveyard(player1, "Toshiro Umezawa");
    }

    @Test
    @DisplayName("Returns a creature you own when it dies under an opponent's control")
    void returnsOwnedCreatureThatDiedUnderOpponentsControl() {
        harness.addToBattlefield(player1, new ShireiShizosCaretaker());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new Frostling());

        threadsSteals(victim);
        harness.assertOnBattlefield(player2, "Frostling");
        harness.assertNotOnBattlefield(player1, "Frostling");

        firstVolleyAt(player2, player2, "Frostling");
        resolveDeathTrigger();

        harness.assertInGraveyard(player1, "Frostling");

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Frostling");
        harness.assertNotInGraveyard(player1, "Frostling");
    }

    @Test
    @DisplayName("A Shirei that leaves and returns is a new object")
    void returnedShireiDoesNotSatisfyPendingReturn() {
        harness.addToBattlefield(player1, new ShireiShizosCaretaker());
        harness.addToBattlefield(player1, new Frostling());

        firstVolleyAt(player1, player1, "Frostling");
        resolveDeathTrigger();
        herosDemiseAt(player1, player1, "Shirei, Shizo's Caretaker");

        Card shireiCard = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Shirei, Shizo's Caretaker"))
                .findFirst()
                .orElseThrow();
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new GoryosVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, shireiCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shirei, Shizo's Caretaker");

        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Frostling");
        harness.assertInGraveyard(player1, "Frostling");
    }

    @Test
    @DisplayName("The end-step return uses the stack and can be stopped by removing Shirei")
    void canRemoveShireiInResponseToDelayedReturn() {
        harness.addToBattlefield(player1, new ShireiShizosCaretaker());
        harness.addToBattlefield(player1, new Frostling());
        firstVolleyAt(player1, player1, "Frostling");
        resolveDeathTrigger();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertInGraveyard(player1, "Frostling");
        harness.assertNotOnBattlefield(player1, "Frostling");
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new HerosDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Shirei, Shizo's Caretaker"));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Frostling");
        harness.assertNotOnBattlefield(player1, "Frostling");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A creature that leaves its graveyard and dies again is a new object")
    void doesNotReturnNewGraveyardObjectFromEarlierDeath() {
        harness.addToBattlefield(player1, new ShireiShizosCaretaker());
        harness.addToBattlefield(player1, new ToshiroUmezawa());
        sickeningShoalForTwoAt("Toshiro Umezawa");
        resolveDeathTrigger();

        Card toshiro = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Toshiro Umezawa"))
                .findFirst().orElseThrow();
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new GoryosVengeance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, toshiro.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Toshiro Umezawa");

        herosDemiseAt(player1, player1, "Toshiro Umezawa");
        resolveAllTriggers();
        advanceToEndStep();

        harness.assertInGraveyard(player1, "Toshiro Umezawa");
        harness.assertNotOnBattlefield(player1, "Toshiro Umezawa");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
