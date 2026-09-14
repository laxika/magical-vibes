package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CradleGuard.class, GorillaWarrior.class})
class CradleGuardTest extends BaseCardTest {

    @Test
    void decliningEchoSacrificesCradleGuard() {
        castAndResolveCradleGuard();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Cradle Guard");
        harness.assertInGraveyard(player1, "Cradle Guard");
    }

    @Test
    void payingEchoKeepsCradleGuardAndEchoIsOneShot() {
        castAndResolveCradleGuard();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Cradle Guard");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Cradle Guard");
    }

    @Test
    void echoDoesNotTriggerDuringOpponentUpkeep() {
        castAndResolveCradleGuard();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Cradle Guard");
    }

    @Test
    void trampleDealsExcessCombatDamage() {
        addCreatureReady(player1, new CradleGuard());
        Permanent blocker = addCreatureReady(player2, new GorillaWarrior());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player2, "Gorilla Warrior");
    }

    private void castAndResolveCradleGuard() {
        harness.setHand(player1, List.of(new CradleGuard()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0, 0);
        resolveAllTriggers();
    }
}
