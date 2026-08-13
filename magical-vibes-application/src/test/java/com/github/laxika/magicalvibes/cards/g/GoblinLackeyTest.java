package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinLackeyTest extends BaseCardTest {

    @Test
    @DisplayName("Noncombat damage presents the may-put-a-Goblin choice")
    void noncombatDamagePresentsMayChoice() {
        dealNoncombatDamage();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the trigger puts a chosen Goblin permanent onto the battlefield")
    void acceptingTriggerPutsGoblinPermanentOntoBattlefield() {
        harness.setHand(player1, List.of(new GoblinHero(), new GrizzlyBears(), new Shock()));
        dealNoncombatDamage();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.HandChoice choice = (PendingInteraction.HandChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Goblin Hero");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("Declining the trigger leaves the hand unchanged")
    void decliningTriggerLeavesHandUnchanged() {
        harness.setHand(player1, List.of(new GoblinHero()));
        dealNoncombatDamage();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Goblin Hero");
        harness.assertNotOnBattlefield(player1, "Goblin Hero");
    }

    private void dealNoncombatDamage() {
        addCreatureReady(player1, new GoblinLackey());
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pyromancer), null,
                player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
