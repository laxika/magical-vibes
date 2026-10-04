package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Grapeshot.class, AshcoatBear.class})
class GrapeshotTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        castGrapeshot(player2.getId());

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature")
    void dealsDamageToCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AshcoatBear()).getId();
        castGrapeshot(targetId);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Grapeshot")
    void stormCreatesCopiesForEachPriorSpell() {
        gd.recordSpellCast(player1.getId(), new AshcoatBear());
        gd.recordSpellCast(player2.getId(), new AshcoatBear());
        castGrapeshot(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    @DisplayName("Storm copies deal damage when their targets are unchanged")
    void stormCopiesDealDamageToOriginalTarget() {
        harness.setLife(player2, 20);
        gd.recordSpellCast(player1.getId(), new AshcoatBear());
        gd.recordSpellCast(player2.getId(), new AshcoatBear());
        castGrapeshot(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("A Storm copy may be retargeted to another player")
    void stormCopyMayChooseNewTargetPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.recordSpellCast(player1.getId(), new AshcoatBear());
        castGrapeshot(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Storm ignores spells cast after Grapeshot")
    void stormCountIsFixedWhenGrapeshotIsCast() {
        gd.recordSpellCast(player1.getId(), new AshcoatBear());
        castGrapeshot(player2.getId());
        gd.recordSpellCast(player2.getId(), new AshcoatBear());

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A Storm copy may target a creature independently of the original spell")
    void stormCopyMayChooseNewTargetCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new AshcoatBear()).getId();
        gd.recordSpellCast(player1.getId(), new AshcoatBear());
        castGrapeshot(player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creatureId);
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Ashcoat Bear").getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 19);
        assertThat(gd.getTotalSpellsCastThisTurnCount()).isEqualTo(2);
    }

    private void castGrapeshot(UUID targetId) {
        harness.setHand(player1, List.of(new Grapeshot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, targetId);
    }

}
