package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeasonOfTheWitch.class, Squire.class})
class SeasonOfTheWitchTest extends BaseCardTest {

    @Test
    void destroysUntappedCreaturesThatCouldAttack() {
        harness.addToBattlefieldAndReturn(player1, new SeasonOfTheWitch());
        Permanent doomed = addCreatureReady(player2, new Squire());
        Permanent unableToAttack = harness.addToBattlefieldAndReturn(player2, new Squire());
        Permanent tapped = addCreatureReady(player2, new Squire());
        tapped.tap();
        Permanent attacked = addCreatureReady(player2, new Squire());
        attacked.setAttackedThisTurn(true);
        Permanent otherPlayerCreature = addCreatureReady(player1, new Squire());

        runEndStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(doomed)
                .contains(unableToAttack, tapped, attacked);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherPlayerCreature);
    }

    @Test
    void doesNotDestroyCreatureThatEnteredAfterDeclareAttackers() {
        harness.addToBattlefieldAndReturn(player1, new SeasonOfTheWitch());

        declareAttackers(player2, List.of());
        Permanent enteredAfterDeclareAttackers = addCreatureReady(player2, new Squire());

        runEndStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enteredAfterDeclareAttackers);
    }

    @Test
    void destroysCreatureThatCouldAttackEvenIfItBecomesUnableLater() {
        harness.addToBattlefieldAndReturn(player1, new SeasonOfTheWitch());
        Permanent creature = addCreatureReady(player2, new Squire());

        declareAttackers(player2, List.of());
        creature.setCantAttackThisTurn(true);

        runEndStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    void paysTwoLifeToKeepTheEnchantment() {
        harness.addToBattlefieldAndReturn(player1, new SeasonOfTheWitch());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        harness.assertOnBattlefield(player1, "Season of the Witch");
    }

    @Test
    void sacrificesTheEnchantmentWhenLifePaymentIsDeclined() {
        harness.addToBattlefieldAndReturn(player1, new SeasonOfTheWitch());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Season of the Witch");
        harness.assertInGraveyard(player1, "Season of the Witch");
    }

    @Test
    void doesNotDestroyCreatureTappedAfterDecliningToAttack() {
        harness.addToBattlefieldAndReturn(player1, new SeasonOfTheWitch());
        Permanent creature = addCreatureReady(player2, new Squire());

        declareAttackers(player2, List.of());
        creature.tap();

        runEndStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void doesNotDestroyCreatureTappedInResponseToEndStepTrigger() {
        harness.addToBattlefieldAndReturn(player1, new SeasonOfTheWitch());
        Permanent creature = addCreatureReady(player2, new Squire());
        declareAttackers(player2, List.of());
        harness.forceStep(TurnStep.MAIN2);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        creature.tap();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void destroysCreatureUntappedInResponseToEndStepTrigger() {
        harness.addToBattlefieldAndReturn(player1, new SeasonOfTheWitch());
        Permanent creature = addCreatureReady(player2, new Squire());
        declareAttackers(player2, List.of());
        creature.tap();
        harness.forceStep(TurnStep.MAIN2);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        creature.untap();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Squire");
    }

    @Test
    void destroysCreatureThatCouldAttackInAnEarlierCombat() {
        harness.addToBattlefieldAndReturn(player1, new SeasonOfTheWitch());
        Permanent creature = addCreatureReady(player2, new Squire());

        declareAttackers(player2, List.of());
        creature.tap();
        declareAttackers(player2, List.of());
        creature.untap();

        runEndStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Squire");
    }

    @Test
    void sparesCreatureTappedDuringCombatEvenIfUntappedLater() {
        harness.addToBattlefieldAndReturn(player1, new SeasonOfTheWitch());
        Permanent creature = addCreatureReady(player2, new Squire());
        creature.tap();

        declareAttackers(player2, List.of());
        creature.untap();

        runEndStep(player2);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void sacrificesWhenControllerCannotPayTwoLife() {
        harness.addToBattlefieldAndReturn(player1, new SeasonOfTheWitch());
        harness.setLife(player1, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertNotOnBattlefield(player1, "Season of the Witch");
        harness.assertInGraveyard(player1, "Season of the Witch");
        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
    }

    @Test
    void doesNotRequirePaymentDuringOpponentsUpkeep() {
        harness.addToBattlefieldAndReturn(player1, new SeasonOfTheWitch());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.assertOnBattlefield(player1, "Season of the Witch");
    }

    private void runEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.MAIN2);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
