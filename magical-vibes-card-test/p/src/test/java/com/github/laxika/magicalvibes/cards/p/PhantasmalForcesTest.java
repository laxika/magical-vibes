package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.n.NecromancersMagemark;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantasmalForces.class})
class PhantasmalForcesTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay {U} sacrifices Phantasmal Forces")
    void decliningPaymentSacrificesCreature() {
        harness.addToBattlefield(player1, new PhantasmalForces());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Phantasmal Forces");
        harness.assertInGraveyard(player1, "Phantasmal Forces");
    }

    @Test
    @DisplayName("Paying {U} keeps Phantasmal Forces on the battlefield")
    void payingKeepsCreature() {
        harness.addToBattlefield(player1, new PhantasmalForces());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Phantasmal Forces");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void acceptingWithOnlyNonBlueManaSacrificesCreature() {
        harness.addToBattlefield(player1, new PhantasmalForces());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Phantasmal Forces");
        harness.assertInGraveyard(player1, "Phantasmal Forces");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new PhantasmalForces());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phantasmal Forces");
    }

    @Test
    void mayDeclineEvenWithBlueManaAvailable() {
        harness.addToBattlefield(player1, new PhantasmalForces());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Phantasmal Forces");
        harness.assertInGraveyard(player1, "Phantasmal Forces");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void eachCopyRequiresItsOwnPayment() {
        harness.addToBattlefield(player1, new PhantasmalForces());
        harness.addToBattlefield(player1, new PhantasmalForces());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Phantasmal Forces");
        harness.assertInGraveyard(player1, "Phantasmal Forces");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, NecromancersMagemark.class})
    @DisplayName("Declining to pay asks for Phantasmal Forces' death replacement before it is sacrificed")
    void decliningAsksForTheDeathReplacementBeforePhantasmalForcesIsSacrificed() {
        Permanent forces = harness.addToBattlefieldAndReturn(player1, new PhantasmalForces());
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.addToBattlefieldAndReturn(player1, new NecromancersMagemark()).setAttachedTo(forces.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder(MAGEMARK_RETURN, "Kalitas, Traitor of Ghet");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOfSatisfying(ChoiceContext.DeathExileReplacementChoice.class,
                        choice -> assertThat(choice.rerunsEffect()).isTrue());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forces);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrific")).isEmpty();

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertThat(gd.playerHands.get(player1.getId())).contains(forces.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forces);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrific")).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, NecromancersMagemark.class})
    @DisplayName("Accepting without blue mana asks for Phantasmal Forces' death replacement before it is sacrificed")
    void acceptingWithoutBlueManaAsksForTheDeathReplacementBeforePhantasmalForcesIsSacrificed() {
        Permanent forces = harness.addToBattlefieldAndReturn(player1, new PhantasmalForces());
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.addToBattlefieldAndReturn(player1, new NecromancersMagemark()).setAttachedTo(forces.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder(MAGEMARK_RETURN, "Kalitas, Traitor of Ghet");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOfSatisfying(ChoiceContext.DeathExileReplacementChoice.class,
                        choice -> assertThat(choice.rerunsEffect()).isTrue());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forces);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrific")).isEmpty();

        harness.handleListChoice(player1, MAGEMARK_RETURN);

        assertThat(gd.playerHands.get(player1.getId())).contains(forces.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forces);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrific")).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private static final String MAGEMARK_RETURN = "Return it to its owner's hand (Necromancer's Magemark)";
}
