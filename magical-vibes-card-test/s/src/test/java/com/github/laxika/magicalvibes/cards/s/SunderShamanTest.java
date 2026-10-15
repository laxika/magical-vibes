package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cindervines;
import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.cards.g.GruulLocket;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunderShaman.class, Cindervines.class, GruulLocket.class, AxebaneBeast.class})
class SunderShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage lets its controller destroy an artifact or enchantment controlled by the damaged player")
    void destroysArtifactOrEnchantmentControlledByDamagedPlayer() {
        Permanent shaman = addCreatureReady(player1, new SunderShaman());
        shaman.setAttacking(true);
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new GruulLocket());
        Permanent enemyArtifact = harness.addToBattlefieldAndReturn(player2, new GruulLocket());
        Permanent enemyEnchantment = harness.addToBattlefieldAndReturn(player2, new Cindervines());
        Permanent enemyCreature = addCreatureReady(player2, new AxebaneBeast());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(enemyArtifact.getId(), enemyEnchantment.getId())
                .doesNotContain(ownArtifact.getId(), enemyCreature.getId());

        harness.handlePermanentChosen(player1, enemyEnchantment.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cindervines");
        harness.assertInGraveyard(player2, "Cindervines");
        harness.assertOnBattlefield(player2, "Gruul Locket");
    }

    @Test
    @DisplayName("It can't be blocked by two creatures")
    void cannotBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new SunderShaman());
        addCreatureReady(player2, new AxebaneBeast());
        addCreatureReady(player2, new AxebaneBeast());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    void destroysArtifactAfterCombatDamage() {
        Permanent shaman = addCreatureReady(player1, new SunderShaman());
        shaman.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GruulLocket());

        resolveCombat();

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gruul Locket");
        harness.assertInGraveyard(player2, "Gruul Locket");
    }

    @Test
    void combatDamageWithNoLegalTargetDoesNotAskForChoice() {
        Permanent shaman = addCreatureReady(player1, new SunderShaman());
        shaman.setAttacking(true);
        harness.addToBattlefield(player1, new GruulLocket());
        addCreatureReady(player2, new AxebaneBeast());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Gruul Locket");
        harness.assertOnBattlefield(player2, "Axebane Beast");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void oneBlockerIsLegalAndPreventsThePlayerDamageTrigger() {
        addCreatureReady(player1, new SunderShaman());
        addCreatureReady(player2, new AxebaneBeast());
        harness.addToBattlefield(player2, new GruulLocket());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Axebane Beast");
        harness.assertOnBattlefield(player1, "Sunder Shaman");
        harness.assertOnBattlefield(player2, "Gruul Locket");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
