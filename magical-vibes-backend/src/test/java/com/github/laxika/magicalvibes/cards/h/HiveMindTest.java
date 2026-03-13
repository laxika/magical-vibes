package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HiveMindTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Hive Mind has correct effect registration")
    void hasCorrectEffects() {
        HiveMind card = new HiveMind();

        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL).get(0))
                .isInstanceOf(CopySpellForEachOtherPlayerEffect.class);
    }

    // ===== Trigger — untargeted sorcery =====

    @Test
    @DisplayName("When a player casts a sorcery, the opponent gets a copy")
    void sorceryCopiedForOpponent() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        // Stack: original sorcery + Hive Mind triggered ability
        assertThat(gd.stack).hasSize(2);
        StackEntry trigger = gd.stack.getLast();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getDescription()).contains("Hive Mind");
    }

    @Test
    @DisplayName("Resolving Hive Mind trigger creates a copy for the opponent")
    void triggerCreatesCopyForOpponent() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        // Resolve Hive Mind triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Stack: original sorcery + copy for player2
        assertThat(gd.stack).hasSize(2);
        StackEntry copyEntry = gd.stack.getLast();
        assertThat(copyEntry.getDescription()).isEqualTo("Copy of Counsel of the Soratami");
        assertThat(copyEntry.isCopy()).isTrue();
        assertThat(copyEntry.getControllerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Copy of draw spell draws cards for the opponent")
    void copyDrawsForOpponent() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        GameData gd = harness.getGameData();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.castSorcery(player1, 0, 0);
        // Resolve Hive Mind triggered ability → creates copy for player2
        harness.passBothPriorities();
        // Resolve copy → player2 draws 2
        harness.passBothPriorities();

        int p2HandAfter = gd.playerHands.get(player2.getId()).size();
        assertThat(p2HandAfter - p2HandBefore).isEqualTo(2);
    }

    @Test
    @DisplayName("Original spell still resolves after copy")
    void originalStillResolves() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        GameData gd = harness.getGameData();
        int p1HandBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, 0);
        // Resolve Hive Mind triggered ability
        harness.passBothPriorities();
        // Resolve copy
        harness.passBothPriorities();
        // Resolve original
        harness.passBothPriorities();

        int p1HandAfter = gd.playerHands.get(player1.getId()).size();
        // player1 cast 1 card (hand -1), then drew 2 (hand +2) = net +1
        assertThat(p1HandAfter - p1HandBefore).isEqualTo(1);
    }

    // ===== Trigger — instant =====

    @Test
    @DisplayName("Hive Mind triggers on instant spells too")
    void triggersOnInstant() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, bearsPermId);

        GameData gd = harness.getGameData();
        // Stack: Lightning Bolt + Hive Mind triggered ability
        assertThat(gd.stack).hasSize(2);
        StackEntry trigger = gd.stack.getLast();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    // ===== Targeted spell — retarget option =====

    @Test
    @DisplayName("Copy of targeted spell offers retarget may-ability")
    void targetedSpellOffersRetarget() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player2, List.of(boomerang));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, bearsPermId);

        // Resolve Hive Mind triggered ability → copy created for player1
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining retarget keeps original target on copy")
    void decliningRetargetKeepsOriginalTarget() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player2, List.of(boomerang));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, bearsPermId);

        // Resolve Hive Mind triggered ability
        harness.passBothPriorities();
        // Decline retarget
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // Find the copy on the stack
        StackEntry copyEntry = gd.stack.stream()
                .filter(se -> se.getDescription().equals("Copy of Boomerang"))
                .findFirst().orElseThrow();
        assertThat(copyEntry.getTargetPermanentId()).isEqualTo(bearsPermId);
        assertThat(copyEntry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting retarget allows choosing a new target for copy")
    void acceptingRetargetChangesTarget() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bears1);
        harness.addToBattlefield(player2, bears2);
        UUID bears1PermId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID bears2PermId = harness.getPermanentId(player2, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player2, List.of(boomerang));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        // Player2 casts Boomerang targeting player1's bears
        harness.castInstant(player2, 0, bears1PermId);

        // Resolve Hive Mind triggered ability → copy for player1
        harness.passBothPriorities();
        // Accept retarget
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose player2's bears as new target
        harness.handlePermanentChosen(player1, bears2PermId);

        StackEntry copyEntry = gd.stack.stream()
                .filter(se -> se.getDescription().equals("Copy of Boomerang"))
                .findFirst().orElseThrow();
        assertThat(copyEntry.getTargetPermanentId()).isEqualTo(bears2PermId);
    }

    // ===== Does not trigger on creature spells =====

    @Test
    @DisplayName("Hive Mind does not trigger on creature spells")
    void doesNotTriggerOnCreature() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        // Stack should only have the creature spell, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Copy does not go to graveyard =====

    @Test
    @DisplayName("Spell copy ceases to exist and does not go to graveyard")
    void copyDoesNotGoToGraveyard() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        // Resolve Hive Mind trigger
        harness.passBothPriorities();
        // Resolve copy
        harness.passBothPriorities();
        // Resolve original
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Copy should not appear in any graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Counsel of the Soratami"));
    }

    // ===== Opponent casting triggers too =====

    @Test
    @DisplayName("Opponent casting a sorcery gives controller a copy")
    void opponentCastingSorceryGivesControllerCopy() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player2, List.of(counsel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 0);
        // Resolve Hive Mind triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Stack should have original + copy for player1
        assertThat(gd.stack).hasSize(2);
        StackEntry copyEntry = gd.stack.getLast();
        assertThat(copyEntry.getDescription()).isEqualTo("Copy of Counsel of the Soratami");
        assertThat(copyEntry.getControllerId()).isEqualTo(player1.getId());
        assertThat(copyEntry.isCopy()).isTrue();
    }

    // ===== Stack is empty after all resolves =====

    @Test
    @DisplayName("Stack is empty after trigger, copy, and original all resolve")
    void stackEmptyAfterFullResolution() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        // Resolve Hive Mind trigger
        harness.passBothPriorities();
        // Resolve copy
        harness.passBothPriorities();
        // Resolve original
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Game log =====

    @Test
    @DisplayName("Game log records copy creation")
    void gameLogRecordsCopyCreation() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        // Resolve Hive Mind trigger
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("copy") && log.contains("Counsel of the Soratami"));
    }

    // ===== Targeted instant — damage copy hits correct target =====

    @Test
    @DisplayName("Copy of Lightning Bolt deals damage controlled by the opponent")
    void copyOfBoltDealsDamageForOpponent() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, bearsPermId);

        // Resolve Hive Mind trigger → copy for player1, may retarget
        harness.passBothPriorities();
        // Decline retarget — copy keeps targeting bears
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        StackEntry copyEntry = gd.stack.stream()
                .filter(se -> se.getDescription().equals("Copy of Lightning Bolt"))
                .findFirst().orElseThrow();
        assertThat(copyEntry.getControllerId()).isEqualTo(player1.getId());
        assertThat(copyEntry.getTargetPermanentId()).isEqualTo(bearsPermId);
    }
}
