package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChurningEddy;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.l.Liquify;
import com.github.laxika.magicalvibes.cards.n.NantukoShade;
import com.github.laxika.magicalvibes.cards.s.Skullscorch;
import com.github.laxika.magicalvibes.cards.s.SeedsOfStrength;
import com.github.laxika.magicalvibes.cards.t.TaintedIsle;
import com.github.laxika.magicalvibes.cards.v.ViolentEruption;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Radiate.class, FieryTemper.class, NantukoShade.class, TaintedIsle.class,
        Skullscorch.class, ChurningEddy.class, Liquify.class, SeedsOfStrength.class,
        ViolentEruption.class})
class RadiateTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a single-target spell for each other legal permanent and player")
    void copiesForEachOtherLegalTarget() {
        FieryTemper fieryTemper = new FieryTemper();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new NantukoShade());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new NantukoShade());
        Permanent nonCreaturePermanent = harness.addToBattlefieldAndReturn(player2, new TaintedIsle());
        harness.setHand(player1, List.of(fieryTemper, new Radiate()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castInstant(player1, 0, ownCreature.getId());
        harness.castAndResolveInstant(player1, 0, fieryTemper.getId());

        List<StackEntry> copies = gd.stack.stream().filter(StackEntry::isCopy).toList();
        assertThat(copies).hasSize(3);
        assertThat(copies).extracting(StackEntry::getTargetId)
                .containsExactlyInAnyOrder(opposingCreature.getId(), player1.getId(), player2.getId());
        assertThat(copies).extracting(StackEntry::getTargetId).doesNotContain(nonCreaturePermanent.getId());
        assertThat(copies).allMatch(copy -> copy.getControllerId().equals(player1.getId()));
    }

    @Test
    @DisplayName("Copies a player-targeting spell for each other player")
    void copiesPlayerTargetingSpellForEachOtherPlayer() {
        Skullscorch skullscorch = new Skullscorch();
        harness.setHand(player1, List.of(skullscorch, new Radiate()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, skullscorch.getId());

        List<StackEntry> copies = gd.stack.stream().filter(StackEntry::isCopy).toList();
        assertThat(copies).hasSize(1);
        assertThat(copies).extracting(StackEntry::getTargetId)
                .containsExactly(player1.getId());
        assertThat(copies).allMatch(copy -> copy.getControllerId().equals(player1.getId()));
    }

    @Test
    @DisplayName("Copies are controlled by Radiate's controller")
    void copiesAreControlledByRadiateController() {
        FieryTemper fieryTemper = new FieryTemper();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NantukoShade());
        harness.setHand(player2, List.of(fieryTemper));
        harness.setHand(player1, List.of(new Radiate()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, fieryTemper.getId());

        List<StackEntry> copies = gd.stack.stream().filter(StackEntry::isCopy).toList();
        assertThat(copies).hasSize(2);
        assertThat(copies).extracting(StackEntry::getTargetId)
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());
        assertThat(copies).allMatch(copy -> copy.getControllerId().equals(player1.getId()));
    }

    @Test
    @DisplayName("Copies resolve against each distinct target")
    void copiesResolveAgainstEachDistinctTarget() {
        FieryTemper fieryTemper = new FieryTemper();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new NantukoShade());
        harness.addToBattlefieldAndReturn(player2, new NantukoShade());
        harness.setHand(player1, List.of(fieryTemper, new Radiate()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castInstant(player1, 0, ownCreature.getId());
        harness.castAndResolveInstant(player1, 0, fieryTemper.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player1, "Nantuko Shade");
        harness.assertNotOnBattlefield(player2, "Nantuko Shade");
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        NantukoShade nantukoShade = new NantukoShade();
        harness.setHand(player1, List.of(nantukoShade, new Radiate()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nantukoShade.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell with multiple targets")
    void cannotTargetMultiTargetSpell() {
        ChurningEddy churningEddy = new ChurningEddy();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NantukoShade());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TaintedIsle());
        harness.setHand(player1, List.of(churningEddy, new Radiate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, List.of(creature.getId(), land.getId()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, churningEddy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell whose target is another spell")
    void cannotTargetSpellTargetingAnotherSpell() {
        FieryTemper fieryTemper = new FieryTemper();
        Liquify liquify = new Liquify();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NantukoShade());
        harness.setHand(player1, List.of(fieryTemper, liquify, new Radiate()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castInstant(player1, 0, creature.getId());
        harness.castInstant(player1, 0, fieryTemper.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, liquify.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Copies a spell that targets the same creature three times")
    void copiesSpellWithRepeatedTargetsOnOneCreature() {
        SeedsOfStrength seeds = new SeedsOfStrength();
        Permanent original = harness.addToBattlefieldAndReturn(player1, new NantukoShade());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new NantukoShade());
        harness.setHand(player1, List.of(seeds, new Radiate()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, List.of(original.getId(), original.getId(), original.getId()));
        harness.castAndResolveInstant(player1, 0, seeds.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
    }

    @Test
    @DisplayName("Copies divided damage assigned to a single target onto every other legal target")
    void copiesDividedDamageSpellWithOneTarget() {
        ViolentEruption eruption = new ViolentEruption();
        Permanent original = harness.addToBattlefieldAndReturn(player1, new NantukoShade());
        harness.addToBattlefield(player2, new NantukoShade());
        harness.setHand(player1, List.of(eruption, new Radiate()));
        harness.addMana(player1, ManaColor.RED, 9);

        harness.castInstant(player1, 0, Map.of(original.getId(), 4));
        harness.castAndResolveInstant(player1, 0, eruption.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
        harness.assertNotOnBattlefield(player1, "Nantuko Shade");
        harness.assertNotOnBattlefield(player2, "Nantuko Shade");
    }

    @Test
    @DisplayName("Still copies the targeted spell after its original creature target leaves")
    void copiesAfterOriginalTargetLeavesBattlefield() {
        FieryTemper originalSpell = new FieryTemper();
        Permanent original = harness.addToBattlefieldAndReturn(player1, new NantukoShade());
        harness.addToBattlefield(player2, new NantukoShade());
        harness.setHand(player1, List.of(originalSpell, new Radiate(), new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 11);

        harness.castInstant(player1, 0, original.getId());
        harness.castInstant(player1, 0, originalSpell.getId());
        harness.castAndResolveInstant(player1, 0, original.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player1, "Nantuko Shade");
        harness.assertNotOnBattlefield(player2, "Nantuko Shade");
    }
}
