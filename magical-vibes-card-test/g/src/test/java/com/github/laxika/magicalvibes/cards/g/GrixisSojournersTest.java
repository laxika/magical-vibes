package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DeadshotMinotaur;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrixisSojourners.class, DeadshotMinotaur.class, Terminate.class})
class GrixisSojournersTest extends BaseCardTest {

    @Test
    @DisplayName("Death trigger targets a graveyard card before resolution and may exile it")
    void diesExilesChosenGraveyardCard() {
        harness.addToBattlefield(player1, new GrixisSojourners());
        Card bait = new DeadshotMinotaur();
        harness.setGraveyard(player2, List.of(bait));

        killWithTerminate();
        chooseTriggerTarget(bait.getId());
        harness.assertInGraveyard(player2, "Deadshot Minotaur");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player2, "Deadshot Minotaur");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bait);
    }

    @Test
    @DisplayName("Declining the death trigger after targeting leaves the card in its graveyard")
    void diesMayExileNothing() {
        harness.addToBattlefield(player1, new GrixisSojourners());
        Card bait = new DeadshotMinotaur();
        harness.setGraveyard(player2, List.of(bait));

        killWithTerminate();
        chooseTriggerTarget(bait.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Deadshot Minotaur");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(bait);
    }

    @Test
    @DisplayName("Death trigger rejects a target outside the graveyards before resolution")
    void diesRejectsNonGraveyardTarget() {
        harness.addToBattlefield(player1, new GrixisSojourners());
        harness.setGraveyard(player2, List.of(new DeadshotMinotaur()));

        killWithTerminate();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(UUID.randomUUID())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling exile trigger resolves separately before the cycling draw")
    void cyclingExilesGraveyardCardAndDraws() {
        harness.setHand(player1, List.of(new GrixisSojourners()));
        harness.setLibrary(player1, List.of(new Terminate()));
        Card bait = new DeadshotMinotaur();
        harness.setGraveyard(player2, List.of(bait));
        payCyclingAndActivate();

        chooseTriggerTarget(bait.getId());
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bait);
        harness.assertInGraveyard(player1, "Grixis Sojourners");
        harness.assertNotInHand(player1, "Terminate");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Terminate");
    }

    @Test
    @DisplayName("Declining the cycling trigger still leaves the separate draw ability")
    void cyclingMayExileNothingStillDraws() {
        harness.setHand(player1, List.of(new GrixisSojourners()));
        harness.setLibrary(player1, List.of(new Terminate()));
        Card bait = new DeadshotMinotaur();
        harness.setGraveyard(player2, List.of(bait));
        payCyclingAndActivate();

        chooseTriggerTarget(bait.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(bait);
        harness.assertInGraveyard(player2, "Deadshot Minotaur");
        harness.assertNotInHand(player1, "Terminate");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Terminate");
    }

    @Test
    @DisplayName("The optional death trigger still requires exactly one target")
    void deathTriggerCannotChooseZeroTargets() {
        harness.addToBattlefield(player1, new GrixisSojourners());
        killWithTerminate();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The death trigger can target and exile Grixis Sojourners itself")
    void deathTriggerCanExileItself() {
        Card sojourners = new GrixisSojourners();
        harness.addToBattlefield(player1, sojourners);
        killWithTerminate();

        chooseTriggerTarget(sojourners.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Grixis Sojourners");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sojourners);
    }

    @Test
    @DisplayName("The cycling trigger can exile the discarded source before the draw")
    void cyclingTriggerCanExileItself() {
        Card sojourners = new GrixisSojourners();
        harness.setHand(player1, List.of(sojourners));
        harness.setLibrary(player1, List.of(new Terminate()));
        payCyclingAndActivate();

        harness.assertInGraveyard(player1, "Grixis Sojourners");
        chooseTriggerTarget(sojourners.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sojourners);
        harness.assertNotInHand(player1, "Terminate");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Terminate");
    }

    @Test
    @DisplayName("An invalid cycling-trigger target does not stop the separate cycling draw")
    void cyclingDrawSurvivesTargetLeavingGraveyard() {
        harness.setHand(player1, List.of(new GrixisSojourners()));
        harness.setLibrary(player1, List.of(new Terminate()));
        Card bait = new DeadshotMinotaur();
        harness.setGraveyard(player2, List.of(bait));
        payCyclingAndActivate();
        chooseTriggerTarget(bait.getId());

        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(bait));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(bait);
        harness.assertInHand(player2, "Deadshot Minotaur");
        harness.assertNotInHand(player1, "Terminate");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Terminate");
    }

    @Test
    @DisplayName("Cycling requires a black mana in addition to two generic mana")
    void cyclingCannotBePaidWithOnlyColorlessMana() {
        harness.setHand(player1, List.of(new GrixisSojourners()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Grixis Sojourners");
        harness.assertNotInGraveyard(player1, "Grixis Sojourners");
        assertThat(gd.stack).isEmpty();
    }

    private void chooseTriggerTarget(UUID targetId) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(targetId));
    }

    private void payCyclingAndActivate() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateHandAbility(player1, 0, null);
    }

    private void killWithTerminate() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Grixis Sojourners"));
    }
}
