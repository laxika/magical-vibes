package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Catalog;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinLackey.class, HermeticStudy.class, GoblinRaider.class, CoralMerfolk.class, Catalog.class})
class GoblinLackeyTest extends BaseCardTest {

    @Test
    @DisplayName("Noncombat damage presents the may-put-a-Goblin choice")
    void noncombatDamagePresentsMayChoice() {
        dealNoncombatDamage();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Combat damage presents the may-put-a-Goblin choice")
    void combatDamagePresentsMayChoice() {
        harness.setHand(player1, List.of(new GoblinRaider()));
        addCreatureReady(player1, new GoblinLackey());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the trigger puts a chosen Goblin permanent onto the battlefield")
    void acceptingTriggerPutsGoblinPermanentOntoBattlefield() {
        harness.setHand(player1, List.of(new GoblinRaider(), new CoralMerfolk(), new Catalog()));
        dealNoncombatDamage();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.HandChoice choice = (PendingInteraction.HandChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Goblin Raider");
        harness.assertInHand(player1, "Coral Merfolk");
        harness.assertInHand(player1, "Catalog");
    }

    @Test
    @DisplayName("Declining the trigger leaves the hand unchanged")
    void decliningTriggerLeavesHandUnchanged() {
        harness.setHand(player1, List.of(new GoblinRaider()));
        dealNoncombatDamage();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Goblin Raider");
        harness.assertNotOnBattlefield(player1, "Goblin Raider");
    }

    @Test
    @DisplayName("Accepting with no Goblin permanent leaves the hand unchanged")
    void acceptingWithNoGoblinPermanentLeavesHandUnchanged() {
        harness.setHand(player1, List.of(new CoralMerfolk(), new Catalog()));
        dealNoncombatDamage();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Coral Merfolk");
        harness.assertInHand(player1, "Catalog");
        harness.assertNotOnBattlefield(player1, "Coral Merfolk");
    }

    private void dealNoncombatDamage() {
        Permanent lackey = addCreatureReady(player1, new GoblinLackey());
        Permanent study = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        study.setAttachedTo(lackey.getId());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lackey), null,
                player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
