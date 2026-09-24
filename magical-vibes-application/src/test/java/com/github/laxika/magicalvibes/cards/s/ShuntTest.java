package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.ConeOfFlame;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        Shunt.class,
        Boomerang.class,
        Cancel.class,
        ConeOfFlame.class,
        CounselOfTheSoratami.class,
        GrizzlyBears.class,
        IcyManipulator.class,
        LavaAxe.class
})
class ShuntTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting Shunt requires targeting a spell with a single target")
    void castingRequiresSingleTargetSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Shunt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, counsel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");
    }

    @Test
    @DisplayName("Casting Shunt rejects a spell with multiple targets")
    void castingRejectsMultipleTargetSpell() {
        UUID target1Id = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID target2Id = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID target3Id = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        ConeOfFlame coneOfFlame = new ConeOfFlame();
        harness.setHand(player1, List.of(coneOfFlame));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new Shunt()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, List.of(target1Id, target2Id, target3Id));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, coneOfFlame.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");
    }

    @Test
    @DisplayName("Casting Shunt cannot target a single-target activated ability")
    void castingRejectsActivatedAbility() {
        IcyManipulator icyManipulator = new IcyManipulator();
        harness.addToBattlefield(player1, icyManipulator);
        UUID bearId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new Shunt()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, bearId);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, icyManipulator.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell");
    }

    @Test
    @DisplayName("Resolving Shunt retargets a single-target spell")
    void resolvingRetargetsSpell() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        UUID bears1PermId = harness.addToBattlefieldAndReturn(player1, bears1).getId();
        UUID bears2PermId = harness.addToBattlefieldAndReturn(player2, bears2).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Shunt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bears1PermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).contains(bears2PermId);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).doesNotContain(bears1PermId);

        harness.handlePermanentChosen(player2, bears2PermId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears2PermId));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears1PermId));
    }

    @Test
    @DisplayName("Shunt does nothing if there is no legal new target")
    void doesNothingWithoutAlternativeTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player1, bears).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Shunt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        StackEntry boomerangEntry = gd.stack.getLast();
        assertThat(boomerangEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(boomerangEntry.getCard().getName()).isEqualTo("Boomerang");
        assertThat(boomerangEntry.getTargetId()).isEqualTo(bearsPermId);
    }

    @Test
    @DisplayName("Shunt can retarget a player-target spell to another legal player")
    void canRetargetPlayerTargetSpell() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new Shunt()));
        harness.addMana(player2, ManaColor.RED, 3);

        GameData gd = harness.getGameData();
        int p1LifeBefore = gd.playerLifeTotals.get(player1.getId());
        int p2LifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lavaAxe.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).contains(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).doesNotContain(player2.getId());

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1LifeBefore - 5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("Shunt can retarget a spell-targeting spell to a different spell on stack")
    void canRetargetSpellTargetingSpell() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        UUID bears1PermId = harness.addToBattlefieldAndReturn(player1, bears1).getId();
        UUID bears2PermId = harness.addToBattlefieldAndReturn(player1, bears2).getId();

        Boomerang boomerangA = new Boomerang();
        Boomerang boomerangB = new Boomerang();
        harness.setHand(player1, List.of(boomerangA, boomerangB));
        harness.addMana(player1, ManaColor.BLUE, 4);

        Cancel cancel = new Cancel();
        Shunt shunt = new Shunt();
        harness.setHand(player2, List.of(cancel, shunt));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player1, 0, bears1PermId);
        harness.castInstant(player1, 0, bears2PermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerangA.getId());
        harness.castInstant(player2, 0, cancel.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player2, boomerangB.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Cancel resolves before Boomerang B and counters it; Boomerang A then resolves.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears1PermId));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears2PermId));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("Boomerang") && log.contains("is countered"));
    }
}

