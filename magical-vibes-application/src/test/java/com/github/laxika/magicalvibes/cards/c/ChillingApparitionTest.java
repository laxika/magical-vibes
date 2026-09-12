package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BogElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChillingApparition.class, BogElemental.class})
class ChillingApparitionTest extends BaseCardTest {

    @Test
    @DisplayName("Regeneration ability grants a regeneration shield")
    void regeneratesThisCreature() {
        Permanent apparition = addCreatureReady(player1, new ChillingApparition());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(apparition.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration ability requires one black mana")
    void regenerationAbilityRequiresBlackMana() {
        Permanent apparition = addCreatureReady(player1, new ChillingApparition());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(apparition.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("A regeneration shield saves this creature from lethal combat damage")
    void regenerationShieldPreventsCombatDestruction() {
        Permanent apparition = addCreatureReady(player1, new ChillingApparition());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        apparition.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BogElemental());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(apparition);
        assertThat(apparition.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Combat damage to a player makes that player discard a card")
    void combatDamageMakesPlayerDiscard() {
        Permanent apparition = addCreatureReady(player1, new ChillingApparition());
        harness.setHand(player2, List.of(new BogElemental(), new BogElemental()));
        apparition.setAttacking(true);

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Combat damage trigger does not happen when the creature is blocked")
    void blockedCombatDamageDoesNotTrigger() {
        Permanent apparition = addCreatureReady(player1, new ChillingApparition());
        apparition.setAttacking(true);
        harness.setHand(player2, List.of(new BogElemental(), new BogElemental()));
        Permanent blocker = addCreatureReady(player2, new BogElemental());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
