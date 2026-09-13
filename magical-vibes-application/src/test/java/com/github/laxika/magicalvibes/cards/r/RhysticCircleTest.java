package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
import com.github.laxika.magicalvibes.cards.s.SearingWind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhysticCircle.class, DivingGriffin.class, SearingWind.class})
class RhysticCircleTest extends BaseCardTest {

    @Test
    @DisplayName("If no player pays, the ability prompts for a source choice")
    void noPlayerPaysPromptsForSource() {
        addReadyCircle(player1);
        Permanent griffin = addReadyGriffin(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateAndDeclinePayment(player1, player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, griffin.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.playerId().equals(player1.getId())
                        && shield.sourceId().equals(griffin.getId()));
    }

    @Test
    @DisplayName("A player paying prevents the source-choice effect")
    void paymentPreventsSourceChoice() {
        addReadyCircle(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("The Circle's controller may pay the optional cost")
    void controllerCanPayOptionalCost() {
        addReadyCircle(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("The chosen source's next damage to the controller is prevented")
    void chosenSourceDamageIsPrevented() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent griffin = addReadyGriffin(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateAndDeclinePayment(player1, player2);
        harness.handlePermanentChosen(player1, griffin.getId());

        griffin.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Damage from the chosen source to a creature you control is not prevented")
    void chosenSourceDamageToControlledCreatureIsNotPrevented() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent blocker = addReadyGriffin(player1);
        Permanent attacker = addReadyGriffin(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateAndDeclinePayment(player1, player2);
        harness.handlePermanentChosen(player1, attacker.getId());

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.sourceId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("A different source still deals damage and leaves the shield unused")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent chosen = addReadyGriffin(player2);
        Permanent other = addReadyGriffin(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateAndDeclinePayment(player1, player2);
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(shield -> shield.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("A spell on the stack can be chosen as the damage source")
    void spellOnStackCanBeChosenAsSource() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        SearingWind searingWind = new SearingWind();
        harness.setHand(player2, List.of(searingWind));
        harness.addMana(player2, ManaColor.COLORLESS, 8);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(searingWind.getId());
        harness.handlePermanentChosen(player1, searingWind.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("The prevention shield expires at the end of the turn")
    void shieldClearedAtEndOfTurn() {
        addReadyCircle(player1);
        Permanent griffin = addReadyGriffin(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        activateAndDeclinePayment(player1, player2);
        harness.handlePermanentChosen(player1, griffin.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private void activateAndDeclinePayment(Player firstPlayer, Player secondPlayer) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(firstPlayer, false);
        harness.handleMayAbilityChosen(secondPlayer, false);
    }

    private void addReadyCircle(Player player) {
        addCreatureReady(player, new RhysticCircle());
    }

    private Permanent addReadyGriffin(Player player) {
        return addCreatureReady(player, new DivingGriffin());
    }
}
