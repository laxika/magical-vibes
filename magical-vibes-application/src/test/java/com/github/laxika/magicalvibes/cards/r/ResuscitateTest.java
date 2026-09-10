package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Resuscitate.class, RagingGoblin.class, HighGround.class})
class ResuscitateTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain the regeneration ability until end of turn")
    void grantsRegenerationAbilityToOwnCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new RagingGoblin());
        Permanent opponentCreature = addCreatureReady(player2, new RagingGoblin());

        castResuscitate();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ownCreature.getRegenerationShield()).isEqualTo(1);
        assertThat(opponentCreature.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("The granted regeneration ability costs one generic mana")
    void grantedRegenerationAbilityRequiresOneGenericMana() {
        Permanent ownCreature = addCreatureReady(player1, new RagingGoblin());

        castResuscitate();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(ownCreature.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Creatures entering later do not gain the regeneration ability")
    void doesNotGrantAbilityToCreaturesEnteringLater() {
        addCreatureReady(player1, new RagingGoblin());
        castResuscitate();

        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new RagingGoblin());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(laterCreature.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("The granted regeneration ability wears off at end of turn")
    void grantedRegenerationAbilityWearsOffAtEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new RagingGoblin());

        castResuscitate();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ownCreature.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Resuscitate cannot grant an ability to a noncreature permanent")
    void doesNotGrantAbilityToNoncreaturePermanent() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new HighGround());

        castResuscitate();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(noncreature.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("The granted regeneration ability saves a creature from lethal combat damage")
    void grantedRegenerationSavesCreatureFromLethalCombatDamage() {
        Permanent blocker = addCreatureReady(player1, new RagingGoblin());
        Permanent attacker = addCreatureReady(player2, new RagingGoblin());

        castResuscitate();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(blocker.isTapped()).isTrue();
        assertThat(blocker.isBlocking()).isFalse();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(blocker.getRegenerationShield()).isZero();
    }

    private void castResuscitate() {
        harness.castFromHand(player1, new Resuscitate(), "{1}{G}");
        harness.passBothPriorities();
    }

}
