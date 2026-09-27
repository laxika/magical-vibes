package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
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

@CardUsed({RadiantPerformer.class, FieryTemper.class, GrizzlyBears.class, ProdigalSorcerer.class})
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
        Permanent sorcererPermanent = new Permanent(sorcerer);
        sorcererPermanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(sorcererPermanent);
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
}
