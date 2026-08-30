package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToskiBearerOfSecretsTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be countered by Cancel")
    void cannotBeCounteredByCancel() {
        ToskiBearerOfSecrets toski = new ToskiBearerOfSecrets();
        harness.setHand(player1, List.of(toski));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, toski.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Toski, Bearer of Secrets");
        harness.assertNotInGraveyard(player1, "Toski, Bearer of Secrets");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Must attack each combat when able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new ToskiBearerOfSecrets());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Draws for each creature you control that deals combat damage to a player")
    void drawsForEachAllyDealingCombatDamage() {
        Permanent toski = addCreatureReady(player1, new ToskiBearerOfSecrets());
        toski.setAttacking(true);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Survives lethal damage because it is indestructible")
    void survivesLethalDamage() {
        Permanent toski = addCreatureReady(player1, new ToskiBearerOfSecrets());
        toski.setMarkedDamage(1);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(toski);
    }
}
