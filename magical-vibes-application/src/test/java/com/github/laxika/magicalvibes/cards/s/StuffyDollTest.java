package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StuffyDoll.class, Shock.class, FugitiveWizard.class})
class StuffyDollTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: deals 1 damage to itself, which reflects 1 damage to the chosen player")
    void tapAbilityReflectsOneDamage() {
        addReadyDoll(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // 1 damage to itself, ON_DEALT_DAMAGE queued
        harness.passBothPriorities(); // reflected damage resolves

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        // 0/1 with 1 damage marked, but indestructible keeps it on the battlefield
        harness.assertOnBattlefield(player1, "Stuffy Doll");
    }

    @Test
    @DisplayName("Damage from an opponent's spell reflects that much damage to the chosen player")
    void spellDamageReflected() {
        addReadyDoll(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID dollId = harness.getPermanentId(player1, "Stuffy Doll");
        harness.castAndResolveInstant(player2, 0, dollId);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Stuffy Doll");
    }

    @Test
    @DisplayName("Combat damage taken reflects that much damage to the chosen player")
    void combatDamageReflected() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent doll = addReadyDoll(player2);

        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        doll.setBlocking(true);
        doll.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // combat damage: doll takes 1, trigger queued
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player2, "Stuffy Doll");
    }

    @Test
    @DisplayName("The controller can choose themselves as Stuffy Doll enters")
    void canChooseControllerOnEntry() {
        harness.castFromHand(player1, new StuffyDoll(), "{5}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPlayerIds()).contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());

        Permanent doll = gd.playerBattlefields.get(player1.getId()).getFirst();
        doll.setSummoningSick(false);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Stuffy Doll");
    }

    @Test
    @DisplayName("The tap ability cannot be activated while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new StuffyDoll());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The tap ability cannot be activated a second time without untapping")
    void cannotActivateWhileTapped() {
        addReadyDoll(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertLife(player2, 19);
    }

    private Permanent addReadyDoll(Player owner) {
        Permanent doll = harness.addToBattlefieldAndReturn(owner, new StuffyDoll());
        doll.setRememberedTargetPlayerId(owner.equals(player1) ? player2.getId() : player1.getId());
        doll.setSummoningSick(false);
        return doll;
    }
}
