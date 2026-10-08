package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({ToskiBearerOfSecrets.class, Cancel.class, GrizzlyBears.class})
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

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
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

    @Test
    @DisplayName("Tapped Toski is not required to attack")
    void tappedToskiNeedNotAttack() {
        Permanent toski = addCreatureReady(player1, new ToskiBearerOfSecrets());
        toski.tap();

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
        assertThat(toski.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Summoning-sick Toski is not required to attack")
    void summoningSickToskiNeedNotAttack() {
        Permanent toski = harness.addToBattlefieldAndReturn(player1, new ToskiBearerOfSecrets());
        toski.setSummoningSick(true);

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
        assertThat(toski.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Opponent's combat damage does not trigger Toski")
    void doesNotDrawForOpponentsCreature() {
        addCreatureReady(player1, new ToskiBearerOfSecrets());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not draw a card")
    void blockedToskiDoesNotDraw() {
        Permanent toski = addCreatureReady(player1, new ToskiBearerOfSecrets());
        addCreatureReady(player2, new GrizzlyBears());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(toski);
    }

    @Test
    @DisplayName("An ally dealing damage draws even when Toski does not attack")
    void drawsForAllyWhileToskiIsTapped() {
        Permanent toski = addCreatureReady(player1, new ToskiBearerOfSecrets());
        toski.tap();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
}
