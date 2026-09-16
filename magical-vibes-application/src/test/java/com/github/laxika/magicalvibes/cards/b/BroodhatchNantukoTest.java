package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroodhatchNantuko.class, Shock.class, ElvishWarrior.class})
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
        var attacker = addCreatureReady(player1, new ElvishWarrior());
        var nantuko = addCreatureReady(player2, new BroodhatchNantuko());

        attacker.setAttacking(true);
        nantuko.setBlocking(true);
        nantuko.addBlockingTarget(0);

        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanents(player2, "Insect")).hasSize(2);
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its morph cost")
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new BroodhatchNantuko()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        var nantuko = findPermanent(player1, "Broodhatch Nantuko");
        assertThat(nantuko.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(nantuko));
        harness.passBothPriorities();

        assertThat(nantuko.isFaceDown()).isFalse();
    }
}
