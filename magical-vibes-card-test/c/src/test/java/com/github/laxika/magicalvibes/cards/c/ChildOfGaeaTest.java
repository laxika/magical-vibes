package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlanchwoodTreefolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChildOfGaea.class, BlanchwoodTreefolk.class})
class ChildOfGaeaTest extends BaseCardTest {

    @Test
    @DisplayName("Child of Gaea survives its upkeep when its controller pays {G}{G}")
    void survivesUpkeepWhenPaymentIsMade() {
        Permanent child = addCreatureReady(player1, new ChildOfGaea());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(child);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Child of Gaea is sacrificed when its controller declines the upkeep payment")
    void isSacrificedWhenUpkeepPaymentIsDeclined() {
        Permanent child = addCreatureReady(player1, new ChildOfGaea());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(child);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(child.getCard().getId()));
    }

    @Test
    @DisplayName("Child of Gaea is sacrificed when its controller accepts but cannot pay")
    void isSacrificedWhenAcceptedPaymentCannotBePaid() {
        Permanent child = addCreatureReady(player1, new ChildOfGaea());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(child);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(child.getCard().getId()));
    }

    @Test
    @DisplayName("Child of Gaea's regeneration ability creates a regeneration shield")
    void regenerationAbilityCreatesShield() {
        Permanent child = addCreatureReady(player1, new ChildOfGaea());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(child.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent child = addCreatureReady(player1, new ChildOfGaea());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(child);
    }

    @Test
    void regenerationCannotPreventUpkeepSacrifice() {
        Permanent child = addCreatureReady(player1, new ChildOfGaea());
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(child.getRegenerationShield()).isEqualTo(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(child);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(child.getCard());
    }

    @Test
    void regenerationSavesBlockingChildFromLethalCombatDamage() {
        Permanent child = addCreatureReady(player1, new ChildOfGaea());
        Permanent attacker = addCreatureReady(player2, new ChildOfGaea());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.CombatDamageAssignment) {
            harness.handleCombatDamageAssigned(player2, 0, Map.of(child.getId(), 7));
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(child);
        assertThat(child.isTapped()).isTrue();
        assertThat(child.getRegenerationShield()).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(attacker.getCard());
        harness.assertLife(player1, 20);
    }

    @Test
    void trampleDealsExcessDamageThroughBlocker() {
        harness.setLife(player2, 20);
        Permanent child = addCreatureReady(player1, new ChildOfGaea());
        Permanent blocker = addCreatureReady(player2, new BlanchwoodTreefolk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 5, player2.getId(), 2));

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(child);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }
}
