package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroodhatchNantuko.class, Shock.class, GrizzlyBears.class})
class BroodhatchNantukoTest extends BaseCardTest {

    @Test
    @DisplayName("Taking 2 damage offers and accepting creates two Insect tokens")
    void acceptingDamageTriggerCreatesThatManyTokens() {
        harness.addToBattlefield(player2, new BroodhatchNantuko());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Broodhatch Nantuko"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanents(player2, "Insect")).hasSize(2);
        harness.assertInGraveyard(player2, "Broodhatch Nantuko");
    }

    @Test
    @DisplayName("Declining the damage trigger creates no Insect tokens")
    void decliningDamageTriggerCreatesNoTokens() {
        harness.addToBattlefield(player2, new BroodhatchNantuko());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Broodhatch Nantuko"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player2, "Insect")).isEmpty();
        harness.assertInGraveyard(player2, "Broodhatch Nantuko");
    }

    @Test
    @DisplayName("Combat damage also uses the amount of damage dealt")
    void combatDamageCreatesThatManyTokens() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new BroodhatchNantuko());

        var attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        var nantuko = gd.playerBattlefields.get(player2.getId()).getFirst();
        nantuko.setSummoningSick(false);
        nantuko.setBlocking(true);
        nantuko.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanents(player2, "Insect")).hasSize(2);
    }
}
