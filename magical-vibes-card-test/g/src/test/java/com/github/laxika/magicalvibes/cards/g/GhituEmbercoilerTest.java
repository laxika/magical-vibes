package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.PsychogenicProbe;
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

@CardUsed({GhituEmbercoiler.class, GrizzlyBears.class, AirElemental.class,
        LightningBolt.class, PsychogenicProbe.class})
class GhituEmbercoilerTest extends BaseCardTest {

    @Test
    @DisplayName("First main phase discard seeks and exiles a random card with greater mana value")
    void discardingSeeksGreaterManaValueCard() {
        harness.addToBattlefield(player1, new GhituEmbercoiler());
        Card discarded = new GrizzlyBears();
        Card equalManaValue = new GrizzlyBears();
        Card greaterManaValue = new AirElemental();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(equalManaValue, greaterManaValue));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(greaterManaValue);
        assertThat(gd.exilePlayPermissions).containsEntry(greaterManaValue.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd).containsKey(greaterManaValue.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equalManaValue);
    }

    @Test
    @DisplayName("Declining the first main phase ability keeps the hand unchanged")
    void decliningDoesNothing() {
        harness.addToBattlefield(player1, new GhituEmbercoiler());
        Card cardInHand = new GrizzlyBears();
        Card libraryCard = new AirElemental();
        harness.setHand(player1, List.of(cardInHand));
        harness.setLibrary(player1, List.of(libraryCard));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardInHand);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("No card is exiled when the library has no greater mana value card")
    void noGreaterCardDoesNothingAfterDiscard() {
        harness.addToBattlefield(player1, new GhituEmbercoiler());
        Card discarded = new GrizzlyBears();
        Card equalManaValue = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(equalManaValue));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equalManaValue);
    }

    @Test
    void seekCompletesDuringTheDiscardAbilityResolution() {
        harness.addToBattlefield(player1, new GhituEmbercoiler());
        Card discarded = new GrizzlyBears();
        Card sought = new AirElemental();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(sought));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(sought);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void seekingDoesNotTriggerShuffleAbilities() {
        harness.addToBattlefield(player1, new GhituEmbercoiler());
        harness.addToBattlefield(player2, new PsychogenicProbe());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        Card sought = new AirElemental();
        harness.setLibrary(player1, List.of(sought));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(sought);
        harness.assertLife(player1, 20);
    }

    @Test
    void emptyHandCannotSeek() {
        harness.addToBattlefield(player1, new GhituEmbercoiler());
        harness.setHand(player1, List.of());
        Card libraryCard = new AirElemental();
        harness.setLibrary(player1, List.of(libraryCard));

        advanceToPrecombatMain(player1);
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void opponentFirstMainPhaseDoesNotTrigger() {
        harness.addToBattlefield(player1, new GhituEmbercoiler());
        Card handCard = new GrizzlyBears();
        harness.setHand(player1, List.of(handCard));

        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handCard);
    }

    @Test
    void soughtCreatureCanBeCastForItsNormalManaCost() {
        harness.addToBattlefield(player1, new GhituEmbercoiler());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        Card sought = new AirElemental();
        harness.setLibrary(player1, List.of(sought));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castFromExile(player1, sought.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Air Elemental");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void prowessBoostsForOwnNoncreatureSpellAndExpiresAtEndOfTurn() {
        Permanent embercoiler = harness.addToBattlefieldAndReturn(player1, new GhituEmbercoiler());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        int originalPower = gqs.getEffectivePower(gd, embercoiler);
        int originalToughness = gqs.getEffectiveToughness(gd, embercoiler);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, embercoiler)).isEqualTo(originalPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, embercoiler)).isEqualTo(originalToughness + 1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, embercoiler)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, embercoiler)).isEqualTo(originalToughness);
    }

    @Test
    void playPermissionLastsThroughTheControllersNextTurn() {
        harness.addToBattlefield(player1, new GhituEmbercoiler());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        Card sought = new AirElemental();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), sought, new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.exilePlayPermissions).containsEntry(sought.getId(), player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gd.exilePlayPermissions).containsEntry(sought.getId(), player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(sought.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sought);
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
