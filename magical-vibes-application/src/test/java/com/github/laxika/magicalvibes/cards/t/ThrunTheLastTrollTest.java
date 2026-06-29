package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpiketailHatchling;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThrunTheLastTrollTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Thrun has cant-be-countered flag and regenerate ability")
    void hasCorrectProperties() {
        ThrunTheLastTroll card = new ThrunTheLastTroll();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasAtLeastOneElementOfType(CantBeCounteredEffect.class);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(RegenerateEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}{G}");
    }

    // ===== Can't be countered =====

    @Test
    @DisplayName("Thrun cannot be countered by Cancel")
    void cannotBeCounteredByCancel() {
        ThrunTheLastTroll thrun = new ThrunTheLastTroll();
        harness.setHand(player1, List.of(thrun));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, thrun.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thrun, the Last Troll"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Thrun, the Last Troll"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cancel"));
    }

    @Test
    @DisplayName("Thrun cannot be countered by counter-unless-pays abilities")
    void cannotBeCounteredByCounterUnlessPays() {
        harness.addToBattlefield(player2, new SpiketailHatchling());

        ThrunTheLastTroll thrun = new ThrunTheLastTroll();
        harness.setHand(player1, List.of(thrun));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, null, thrun.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thrun, the Last Troll"));
    }

    // ===== Hexproof =====

    @Test
    @DisplayName("Opponent cannot target Thrun with spells")
    void opponentCannotTargetWithSpells() {
        Permanent thrunPerm = addThrunReady(player1);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, thrunPerm.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can target own Thrun with spells")
    void controllerCanTargetOwnThrun() {
        Permanent thrunPerm = addThrunReady(player1);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, thrunPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
    }

    @Test
    @DisplayName("Thrun has hexproof keyword on the battlefield")
    void hasHexproofKeyword() {
        Permanent thrunPerm = addThrunReady(player1);

        assertThat(gqs.hasKeyword(gd, thrunPerm, Keyword.HEXPROOF)).isTrue();
    }

    // ===== Regenerate activated ability =====

    @Test
    @DisplayName("Activating regeneration puts it on the stack")
    void activatingRegenPutsOnStack() {
        Permanent thrunPerm = addThrunReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(thrunPerm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addThrunReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent thrun = findPermanent(player1, "Thrun, the Last Troll");
        assertThat(thrun.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Thrun from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent thrunPerm = addThrunReady(player1);
        thrunPerm.setRegenerationShield(1);
        thrunPerm.setBlocking(true);
        thrunPerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thrun, the Last Troll"));
        Permanent thrun = findPermanent(player1, "Thrun, the Last Troll");
        assertThat(thrun.isTapped()).isTrue();
        assertThat(thrun.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Thrun dies without regeneration shield")
    void diesWithoutRegenShield() {
        Permanent thrunPerm = addThrunReady(player1);
        thrunPerm.setBlocking(true);
        thrunPerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Thrun, the Last Troll"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Thrun, the Last Troll"));
    }

    // ===== Helper methods =====

    private Permanent addThrunReady(Player player) {
        ThrunTheLastTroll card = new ThrunTheLastTroll();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

}
