package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EzuriRenegadeLeader.class, LlanowarElves.class, GrizzlyBears.class})
class EzuriRenegadeLeaderTest extends BaseCardTest {

    @Test
    @DisplayName("Regenerate ability targets another Elf and grants regeneration shield")
    void regenerateTargetsAnotherElf() {
        addCreatureReady(player1, new EzuriRenegadeLeader());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, elf.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(elf.getId());

        harness.passBothPriorities();

        assertThat(elf.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regenerate ability cannot target Ezuri itself")
    void cannotRegenerateItself() {
        Permanent ezuri = addCreatureReady(player1, new EzuriRenegadeLeader());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ezuri.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regenerate ability cannot target a non-Elf creature")
    void cannotRegenerateNonElf() {
        addCreatureReady(player1, new EzuriRenegadeLeader());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regenerate ability can target an opponent's Elf")
    void canRegenerateOpponentElf() {
        addCreatureReady(player1, new EzuriRenegadeLeader());
        Permanent opponentElf = addCreatureReady(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, opponentElf.getId());
        harness.passBothPriorities();

        assertThat(opponentElf.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Overrun gives +3/+3 and trample to Elf creatures you control")
    void overrunBoostsElves() {
        Permanent ezuri = addCreatureReady(player1, new EzuriRenegadeLeader());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Ezuri (2/2 Elf) gets +3/+3 = 5/5
        assertThat(gqs.getEffectivePower(gd, ezuri)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ezuri)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, ezuri, Keyword.TRAMPLE)).isTrue();

        // Llanowar Elves (1/1 Elf) gets +3/+3 = 4/4
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Overrun does not affect non-Elf creatures")
    void overrunDoesNotAffectNonElves() {
        addCreatureReady(player1, new EzuriRenegadeLeader());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Grizzly Bears (2/2 non-Elf) should NOT be boosted
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Overrun does not affect opponent's Elf creatures")
    void overrunDoesNotAffectOpponentElves() {
        addCreatureReady(player1, new EzuriRenegadeLeader());
        Permanent opponentElf = addCreatureReady(player2, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Opponent's Llanowar Elves (1/1) should NOT be boosted
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentElf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Overrun requires 5 mana (2GGG)")
    void overrunRequiresEnoughMana() {
        addCreatureReady(player1, new EzuriRenegadeLeader());
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Overrun affects Elves present at resolution, but not Elves entering afterward")
    void overrunLocksInCreaturesAtResolution() {
        addCreatureReady(player1, new EzuriRenegadeLeader());
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        Permanent earlyElf = addCreatureReady(player1, new LlanowarElves());

        harness.passBothPriorities();
        Permanent lateElf = addCreatureReady(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, earlyElf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, earlyElf)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, earlyElf, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, lateElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lateElf)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, lateElf, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Multiple Overrun activations stack and expire at end of turn")
    void repeatedOverrunExpiresAtCleanup() {
        Permanent ezuri = addCreatureReady(player1, new EzuriRenegadeLeader());
        harness.addMana(player1, ManaColor.GREEN, 10);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ezuri)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, ezuri)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, ezuri, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ezuri)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ezuri)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ezuri, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Overrun resolves even if Ezuri leaves the battlefield")
    void overrunResolvesWithoutSource() {
        Permanent ezuri = addCreatureReady(player1, new EzuriRenegadeLeader());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(ezuri);
        gd.playerGraveyards.get(player1.getId()).add(ezuri.getCard());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Unused regeneration shields accumulate and expire at end of turn")
    void regenerationShieldsExpireAtCleanup() {
        addCreatureReady(player1, new EzuriRenegadeLeader());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, elf.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, elf.getId());
        harness.passBothPriorities();

        assertThat(elf.getRegenerationShield()).isEqualTo(2);
        assertThat(elf.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elf.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Regeneration saves another Elf from lethal combat damage")
    void regenerationPreventsCombatDestruction() {
        addCreatureReady(player1, new EzuriRenegadeLeader());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, elf.getId());
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elf);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(elf.getCard());
        assertThat(elf.getRegenerationShield()).isZero();
        assertThat(elf.isTapped()).isTrue();
        assertThat(elf.isAttacking()).isFalse();
    }
}
