package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.SeedsOfStrength;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadiantPerformer.class, FieryTemper.class, GrizzlyBears.class, ProdigalSorcerer.class,
        SeedsOfStrength.class})
class RadiantPerformerTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a single-target spell for each other legal permanent and player")
    void copiesTargetedSpellForEachOtherLegalTarget() {
        FieryTemper fieryTemper = new FieryTemper();
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(fieryTemper, new RadiantPerformer()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castInstant(player1, 0, originalTarget.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(fieryTemper.getId());
        harness.handlePermanentChosen(player1, fieryTemper.getId());
        harness.passBothPriorities();

        List<StackEntry> copies = gd.stack.stream().filter(StackEntry::isCopy).toList();
        UUID radiantPerformerId = harness.getPermanentId(player1, "Radiant Performer");
        assertThat(copies).hasSize(4);
        assertThat(copies).extracting(StackEntry::getTargetId)
                .containsExactlyInAnyOrder(otherCreature.getId(), radiantPerformerId,
                        player1.getId(), player2.getId());
        assertThat(copies).allMatch(copy -> copy.getControllerId().equals(player1.getId()));
    }

    @Test
    @DisplayName("Copies a single-target activated ability for each other legal permanent and player")
    void copiesTargetedActivatedAbilityForEachOtherLegalTarget() {
        ProdigalSorcerer sorcerer = new ProdigalSorcerer();
        Permanent sorcererPermanent = addCreatureReady(player2, sorcerer);
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, originalTarget.getId());
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new RadiantPerformer()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(sorcerer.getId());
        harness.handlePermanentChosen(player1, sorcerer.getId());
        harness.passBothPriorities();

        List<StackEntry> copies = gd.stack.stream()
                .filter(StackEntry::isCopy)
                .filter(entry -> entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY)
                .toList();
        UUID radiantPerformerId = harness.getPermanentId(player1, "Radiant Performer");
        assertThat(copies).hasSize(5);
        assertThat(copies).extracting(StackEntry::getTargetId)
                .containsExactlyInAnyOrder(sorcererPermanent.getId(), otherCreature.getId(),
                        radiantPerformerId, player1.getId(), player2.getId());
        assertThat(copies).extracting(StackEntry::getTargetId).doesNotContain(originalTarget.getId());
        assertThat(copies).allMatch(copy -> copy.getControllerId().equals(player1.getId()));
    }

    @Test
    @DisplayName("Copies a spell whose three target slots all name the same creature")
    void copiesRepeatedTargetsOnOneCreature() {
        SeedsOfStrength seeds = new SeedsOfStrength();
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(seeds, new RadiantPerformer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, List.of(originalTarget.getId(), originalTarget.getId(), originalTarget.getId()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(seeds.getId());
        harness.handlePermanentChosen(player1, seeds.getId());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy).toList()).hasSize(2);
        Permanent performer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof RadiantPerformer).findFirst().orElseThrow();
        for (int i = 0; i < 3; i++) {
            harness.passBothPriorities();
        }
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, originalTarget)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, performer)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not copy a spell targeting different creatures")
    void doesNotCopyDistinctTargets() {
        SeedsOfStrength seeds = new SeedsOfStrength();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(seeds, new RadiantPerformer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, List.of(first.getId(), first.getId(), second.getId()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
    }

    @Test
    @DisplayName("Entering without being cast from hand does not trigger copying")
    void enteringWithoutCastingDoesNotCopy() {
        FieryTemper fieryTemper = new FieryTemper();
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(fieryTemper));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, originalTarget.getId());

        harness.enterBattlefieldAndReturn(player1, new RadiantPerformer());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
    }
}
