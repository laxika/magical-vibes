package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlchemistsRetrieval;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({StensiaUprising.class, Mountain.class, AlchemistsRetrieval.class})
class StensiaUprisingTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Human token at the beginning of the controller's end step")
    void createsHumanTokenAtEndStep() {
        Permanent uprising = harness.addToBattlefieldAndReturn(player1, new StensiaUprising());

        resolveEndStepTrigger(player1);

        assertThat(findPermanents(player1, "Human")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(uprising);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("At exactly thirteen permanents, may sacrifice Stensia Uprising to deal 7 damage")
    void sacrificesAtExactlyThirteenPermanents() {
        Permanent uprising = addUprisingWithElevenOtherPermanents();
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, uprising.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        harness.assertInGraveyard(player1, "Stensia Uprising");
        assertThat(findPermanents(player1, "Human")).hasSize(1);
    }

    @Test
    @DisplayName("Declining the sacrifice leaves Stensia Uprising on the battlefield")
    void mayDeclineSacrifice() {
        Permanent uprising = addUprisingWithElevenOtherPermanents();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(uprising);
        assertThat(findPermanents(player1, "Human")).hasSize(1);
    }

    @Test
    @DisplayName("Does not offer the sacrifice when the token makes fourteen permanents")
    void doesNotSacrificeWithMoreThanThirteenPermanents() {
        Permanent uprising = harness.addToBattlefieldAndReturn(player1, new StensiaUprising());
        for (int i = 0; i < 12; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }

        resolveEndStepTrigger(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(uprising);
        assertThat(findPermanents(player1, "Human")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not create a token during the opponent's end step")
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new StensiaUprising());

        resolveEndStepTrigger(player2);

        assertThat(findPermanents(player1, "Human")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's permanents do not count toward thirteen")
    void countsOnlyControllersPermanents() {
        Permanent uprising = addUprisingWithElevenOtherPermanents();
        harness.addToBattlefield(player2, new Mountain());

        resolveEndStepTrigger(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(uprising).hasSize(13);
        assertThat(findPermanents(player1, "Human")).hasSize(1);
    }

    @Test
    @DisplayName("Checks the permanent count on resolution, after creating the token")
    void checksCountAtResolution() {
        Permanent uprising = harness.addToBattlefieldAndReturn(player1, new StensiaUprising());
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }

        advanceToEndStep(player1);
        harness.addToBattlefield(player1, new Mountain());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(uprising).hasSize(13);
        assertThat(findPermanents(player1, "Human")).hasSize(1);
    }

    @Test
    @DisplayName("The sacrifice creates a separate damage trigger that can target the new Human")
    void damageUsesSeparateTriggerAndCanTargetCreature() {
        Permanent uprising = addUprisingWithElevenOtherPermanents();

        resolveEndStepTrigger(player1);
        Permanent human = findPermanent(player1, "Human");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, uprising.getId());
        harness.assertInGraveyard(player1, "Stensia Uprising");
        harness.handlePermanentChosen(player1, human.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(human);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(human);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Removing the enchantment in response does not stop token creation or cause damage")
    void removedSourceStillCreatesTokenWithoutDamage() {
        Permanent uprising = addUprisingWithElevenOtherPermanents();
        harness.setHand(player1, List.of(new AlchemistsRetrieval()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        advanceToEndStep(player1);
        harness.castAndResolveInstant(player1, 0, uprising.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Stensia Uprising");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(uprising).hasSize(12);
        assertThat(findPermanents(player1, "Human")).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private Permanent addUprisingWithElevenOtherPermanents() {
        Permanent uprising = harness.addToBattlefieldAndReturn(player1, new StensiaUprising());
        for (int i = 0; i < 11; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        return uprising;
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }

    private void resolveEndStepTrigger(Player player) {
        advanceToEndStep(player);
        harness.passBothPriorities();
    }
}
