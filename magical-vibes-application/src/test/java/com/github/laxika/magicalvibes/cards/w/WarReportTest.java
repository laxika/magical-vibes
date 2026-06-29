package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoxOpal;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.GainLifePerMatchingPermanentOnBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WarReportTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("War Report has correct effects")
    void hasCorrectEffects() {
        WarReport card = new WarReport();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(GainLifePerMatchingPermanentOnBattlefieldEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting War Report puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("War Report");
    }

    // ===== Life gain =====

    @Test
    @DisplayName("Gains life equal to creature count plus artifact count")
    void gainsLifeForCreaturesAndArtifacts() {
        // 2 creatures + 1 artifact = 3 life
        addCreature(player1);
        addCreature(player2);
        addArtifact(player1);

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Artifact creatures are counted twice — once as creature, once as artifact")
    void artifactCreatureCountedTwice() {
        // 1 artifact creature counts as both creature and artifact = 2 life
        addArtifactCreature(player1);

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Counts permanents from all players' battlefields")
    void countsBothPlayersBattlefields() {
        // Player 1: 1 creature, 1 artifact. Player 2: 2 creatures, 1 artifact = 5 life
        addCreature(player1);
        addArtifact(player1);
        addCreature(player2);
        addCreature(player2);
        addArtifact(player2);

        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Gains no life when no creatures or artifacts on the battlefield")
    void gainsNoLifeWhenEmpty() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Only creatures count — gains life with only creatures on battlefield")
    void gainsLifeWithOnlyCreatures() {
        addCreature(player1);
        addCreature(player1);

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Only artifacts count — gains life with only artifacts on battlefield")
    void gainsLifeWithOnlyArtifacts() {
        addArtifact(player1);
        addArtifact(player2);

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new WarReport()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    // ===== Helper methods =====

    private Permanent addCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addArtifact(Player player) {
        MoxOpal card = new MoxOpal();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addArtifactCreature(Player player) {
        WallOfTanglecord card = new WallOfTanglecord();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
