package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NecromancersMagemark;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lithophage.class, Mountain.class, Forest.class})
class LithophageTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep without a Mountain sacrifices Lithophage")
    void upkeepWithoutMountainSacrificesLithophage() {
        harness.addToBattlefield(player1, new Lithophage());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lithophage");
        harness.assertInGraveyard(player1, "Lithophage");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A non-Mountain land does not satisfy the upkeep cost")
    void nonMountainLandDoesNotSatisfyUpkeepCost() {
        harness.addToBattlefield(player1, new Lithophage());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lithophage");
        harness.assertInGraveyard(player1, "Lithophage");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Upkeep with a Mountain asks whether to sacrifice it")
    void upkeepWithMountainPromptsForSacrifice() {
        harness.addToBattlefield(player1, new Lithophage());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Sacrificing a Mountain keeps Lithophage")
    void sacrificingMountainKeepsLithophage() {
        harness.addToBattlefield(player1, new Lithophage());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Mountain").getId());

        harness.assertOnBattlefield(player1, "Lithophage");
        harness.assertNotOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("Declining to sacrifice a Mountain sacrifices Lithophage")
    void decliningMountainSacrificeSacrificesLithophage() {
        harness.addToBattlefield(player1, new Lithophage());
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Lithophage");
        harness.assertInGraveyard(player1, "Lithophage");
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("An opponent's Mountain does not satisfy the upkeep cost")
    void opponentMountainDoesNotSatisfyUpkeepCost() {
        harness.addToBattlefield(player1, new Lithophage());
        harness.addToBattlefield(player2, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lithophage");
        harness.assertInGraveyard(player1, "Lithophage");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Lithophage does not trigger during an opponent's upkeep")
    void opponentUpkeepDoesNotTrigger() {
        harness.addToBattlefield(player1, new Lithophage());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Lithophage");
        harness.assertNotInGraveyard(player1, "Lithophage");
    }

    @Test
    @DisplayName("The controller chooses exactly one Mountain to sacrifice")
    void choosesExactlyOneMountain() {
        harness.addToBattlefield(player1, new Lithophage());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());
        var mountains = findPermanents(player1, "Mountain");
        var chosenMountain = mountains.get(1);

        advanceToUpkeep(player1);
        chosenMountain.tap();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, chosenMountain.getId());

        harness.assertOnBattlefield(player1, "Lithophage");
        assertThat(findPermanents(player1, "Mountain"))
                .extracting(permanent -> permanent.getId())
                .containsExactly(mountains.get(0).getId());
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, NecromancersMagemark.class})
    @DisplayName("Declining to sacrifice a Mountain asks for Lithophage's death replacement before it is sacrificed")
    void decliningAsksForTheDeathReplacementBeforeLithophageIsSacrificed() {
        Permanent lithophage = harness.addToBattlefieldAndReturn(player1, new Lithophage());
        enchantWithMagemarkAgainstKalitas(lithophage);
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertDeathReplacementAskedWhileLithophageStays(lithophage);

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertLithophageReturnedToHandWithoutZombie(lithophage);
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, NecromancersMagemark.class})
    @DisplayName("Accepting with no Mountain left asks for Lithophage's death replacement before it is sacrificed")
    void acceptingWithNothingLeftAsksForTheDeathReplacementBeforeLithophageIsSacrificed() {
        Permanent lithophage = harness.addToBattlefieldAndReturn(player1, new Lithophage());
        enchantWithMagemarkAgainstKalitas(lithophage);
        harness.addToBattlefield(player1, new Mountain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Mountain"));
        harness.handleMayAbilityChosen(player1, true);

        assertDeathReplacementAskedWhileLithophageStays(lithophage);

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertLithophageReturnedToHandWithoutZombie(lithophage);
    }

    private static final String MAGEMARK_RETURN = "Return it to its owner's hand (Necromancer's Magemark)";

    private void assertDeathReplacementAskedWhileLithophageStays(Permanent creature) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder(MAGEMARK_RETURN, "Kalitas, Traitor of Ghet");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOfSatisfying(ChoiceContext.DeathExileReplacementChoice.class,
                        choice -> assertThat(choice.inDepartureBatch()).isTrue());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrific")).isEmpty();
    }

    private void assertLithophageReturnedToHandWithoutZombie(Permanent creature) {
        assertThat(gd.playerHands.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void enchantWithMagemarkAgainstKalitas(Permanent creature) {
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.addToBattlefieldAndReturn(player1, new NecromancersMagemark()).setAttachedTo(creature.getId());
    }
}
