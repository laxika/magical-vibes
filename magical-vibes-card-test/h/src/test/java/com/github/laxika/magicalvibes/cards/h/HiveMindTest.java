package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BlackSunsZenith;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shunt;
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

@CardUsed({HiveMind.class, BlackSunsZenith.class, Boomerang.class, CounselOfTheSoratami.class,
        Fireball.class, GrizzlyBears.class, LightningBolt.class, Shunt.class})
class HiveMindTest extends BaseCardTest {

    @Test
    @DisplayName("The copy uses the original spell's targets when Hive Mind resolves")
    void copyUsesTargetsChangedInResponse() {
        harness.addToBattlefield(player1, new HiveMind());
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new LightningBolt(), new Shunt()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, player2.getId());
        UUID boltId = gd.stack.getFirst().getTargetableId();
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, boltId);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bearsId);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bearsId);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(bearsId);
    }

    @Test
    @DisplayName("The opponent may choose new targets for a multi-target Fireball copy")
    void multiTargetCopyOffersRetarget() {
        harness.addToBattlefield(player1, new HiveMind());
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castSorcery(player1, 0, 6, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A Fireball copy retains X and its targets without triggering Hive Mind again")
    void copyRetainsXAndDoesNotTriggerAgain() {
        harness.addToBattlefield(player1, new HiveMind());
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castSorcery(player1, 0, 6, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player2, false);
        }
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.passBothPriorities();
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 14);
        assertThat(gd.stack).isEmpty();
    }

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

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveSorcery(player1, 0, 0);
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

        harness.castAndResolveSorcery(player1, 0, 0);
        // Resolve copy
        harness.passBothPriorities();
        // Resolve original
        harness.passBothPriorities();

        int p1HandAfter = gd.playerHands.get(player1.getId()).size();
        // player1 cast 1 card (hand -1), then drew 2 (hand +2) = net +1
        assertThat(p1HandAfter - p1HandBefore).isEqualTo(1);
    }

    @Test
    @DisplayName("Hive Mind triggers on instant spells too")
    void triggersOnInstant() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        GrizzlyBears bears = new GrizzlyBears();
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player1, bears).getId();

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

    @Test
    @DisplayName("Copy of targeted spell offers retarget may-ability")
    void targetedSpellOffersRetarget() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        GrizzlyBears bears = new GrizzlyBears();
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player1, bears).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player2, List.of(boomerang));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, bearsPermId);

        // Resolve Hive Mind triggered ability → copy created for player1
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining retarget keeps original target on copy")
    void decliningRetargetKeepsOriginalTarget() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        GrizzlyBears bears = new GrizzlyBears();
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player1, bears).getId();

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
        assertThat(copyEntry.getTargetId()).isEqualTo(bearsPermId);
        assertThat(copyEntry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting retarget allows choosing a new target for copy")
    void acceptingRetargetChangesTarget() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        UUID bears1PermId = harness.addToBattlefieldAndReturn(player1, bears1).getId();
        UUID bears2PermId = harness.addToBattlefieldAndReturn(player2, bears2).getId();

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
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose player2's bears as new target
        harness.handlePermanentChosen(player1, bears2PermId);

        StackEntry copyEntry = gd.stack.stream()
                .filter(se -> se.getDescription().equals("Copy of Boomerang"))
                .findFirst().orElseThrow();
        assertThat(copyEntry.getTargetId()).isEqualTo(bears2PermId);
    }

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

    @Test
    @DisplayName("Spell copy ceases to exist and does not go to graveyard")
    void copyDoesNotGoToGraveyard() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        // Resolve copy
        harness.passBothPriorities();
        // Resolve original
        harness.passBothPriorities();

        // Copy should not appear in any graveyard
        harness.assertNotInGraveyard(player2, "Counsel of the Soratami");
    }

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

    @Test
    @DisplayName("Stack is empty after trigger, copy, and original all resolve")
    void stackEmptyAfterFullResolution() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        // Resolve copy
        harness.passBothPriorities();
        // Resolve original
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Game log records copy creation")
    void gameLogRecordsCopyCreation() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("copy") && log.contains("Counsel of the Soratami"));
    }

    @Test
    @DisplayName("Copy of Black Sun's Zenith does not get shuffled into opponent's library (CR 707.10a)")
    void copyOfShuffleIntoLibrarySpellDoesNotPolluteDeck() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        BlackSunsZenith bsz = new BlackSunsZenith();
        harness.setHand(player1, List.of(bsz));
        harness.addMana(player1, ManaColor.BLACK, 3); // X=1

        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castSorcery(player1, 0, 1);
        // Resolve Hive Mind triggered ability → creates copy for player2
        harness.passBothPriorities();
        // Resolve copy (controlled by player2)
        harness.passBothPriorities();

        // Copy ceases to exist per CR 707.10a — must NOT be in player2's library
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore);
        harness.assertNotInGraveyard(player2, "Black Sun's Zenith");

        // Resolve original → shuffled into player1's library (correct behavior)
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Black Sun's Zenith"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Copy of Lightning Bolt deals damage controlled by the opponent")
    void copyOfBoltDealsDamageForOpponent() {
        HiveMind hiveMind = new HiveMind();
        harness.addToBattlefield(player1, hiveMind);

        GrizzlyBears bears = new GrizzlyBears();
        UUID bearsPermId = harness.addToBattlefieldAndReturn(player1, bears).getId();

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
        assertThat(copyEntry.getTargetId()).isEqualTo(bearsPermId);
    }
}
