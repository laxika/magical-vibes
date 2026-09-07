package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommandoRaid.class, AirElemental.class, GrizzlyBears.class, WallOfAir.class})
class CommandoRaidTest extends BaseCardTest {

    @Test
    @DisplayName("Grants a combat-damage trigger that may deal damage equal to the creature's power")
    void combatDamageTriggerDealsPowerDamageToDamagedPlayersCreature() {
        Permanent attacker = addCreatureReady(player1, new AirElemental());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent damagedPlayersCreature = addCreatureReady(player2, new WallOfAir());
        castOn(attacker);

        attacker.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(damagedPlayersCreature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(damagedPlayersCreature.getId()));

        assertThat(damagedPlayersCreature.getMarkedDamage()).isEqualTo(4);
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining the granted combat-damage trigger deals no additional damage")
    void decliningCombatDamageTriggerDealsNoAdditionalDamage() {
        Permanent attacker = addCreatureReady(player1, new AirElemental());
        Permanent damagedPlayersCreature = addCreatureReady(player2, new WallOfAir());
        castOn(attacker);

        attacker.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(damagedPlayersCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The granted combat-damage trigger expires at end of turn")
    void grantedTriggerExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new AirElemental());
        Permanent damagedPlayersCreature = addCreatureReady(player2, new WallOfAir());
        castOn(attacker);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        attacker.setAttacking(true);
        resolveCombat();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(damagedPlayersCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CommandoRaid()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new CommandoRaid()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
