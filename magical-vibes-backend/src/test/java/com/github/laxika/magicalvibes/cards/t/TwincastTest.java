package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Bandage;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.d.Discombobulate;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.filter.SpellTypeTargetFilter;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TwincastTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Twincast has correct card properties")
    void hasCorrectProperties() {
        Twincast card = new Twincast();

        assertThat(card.getName()).isEqualTo("Twincast");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{U}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.isNeedsSpellTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(CopySpellEffect.class);
        assertThat(card.getTargetFilter()).isInstanceOf(SpellTypeTargetFilter.class);
        SpellTypeTargetFilter filter = (SpellTypeTargetFilter) card.getTargetFilter();
        assertThat(filter.spellTypes()).containsExactlyInAnyOrder(
                StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Twincast puts it on the stack targeting a spell")
    void castingPutsOnStackTargetingSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);

        UUID counselCardId = counsel.getId();
        harness.castInstant(player2, 0, counselCardId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry twincastEntry = gd.stack.getLast();
        assertThat(twincastEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(twincastEntry.getCard().getName()).isEqualTo("Twincast");
        assertThat(twincastEntry.getTargetPermanentId()).isEqualTo(counselCardId);
    }

    @Test
    @DisplayName("Cannot target a creature spell with Twincast")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        UUID bearsCardId = bears.getId();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, bearsCardId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Resolving — copying a sorcery =====

    @Test
    @DisplayName("Resolving creates a copy of the target sorcery on the stack")
    void resolvingCreatesCopyOnStack() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());

        // Resolve Twincast — should create a copy on stack
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Original counsel + copy should be on the stack
        assertThat(gd.stack).hasSize(2);
        StackEntry copyEntry = gd.stack.getLast();
        assertThat(copyEntry.getDescription()).isEqualTo("Copy of Counsel of the Soratami");
        assertThat(copyEntry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(copyEntry.isCopy()).isTrue();
        assertThat(copyEntry.getControllerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Copy of draw spell makes the copy controller draw cards")
    void copyOfDrawSpellDrawsForCopyController() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());

        GameData gd = harness.getGameData();
        // Capture after both spells are cast (hands are now empty)
        int p2HandAfterCast = gd.playerHands.get(player2.getId()).size();

        // Resolve Twincast
        harness.passBothPriorities();
        // Resolve copy of Counsel — player2 draws 2
        harness.passBothPriorities();

        int p2HandAfter = gd.playerHands.get(player2.getId()).size();
        assertThat(p2HandAfter - p2HandAfterCast).isEqualTo(2);
    }

    @Test
    @DisplayName("Original spell still resolves after copy resolves")
    void originalSpellStillResolves() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());

        GameData gd = harness.getGameData();
        // Capture after both spells are cast (player1's hand is now empty)
        int p1HandAfterCast = gd.playerHands.get(player1.getId()).size();

        // Resolve Twincast → copy created
        harness.passBothPriorities();
        // Resolve copy → player2 draws 2
        harness.passBothPriorities();
        // Resolve original → player1 draws 2
        harness.passBothPriorities();

        int p1HandAfter = gd.playerHands.get(player1.getId()).size();
        assertThat(p1HandAfter - p1HandAfterCast).isEqualTo(2);
    }

    // ===== Copy does not go to graveyard =====

    @Test
    @DisplayName("Spell copy ceases to exist and does not go to any graveyard")
    void copyDoesNotGoToGraveyard() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());

        // Resolve Twincast
        harness.passBothPriorities();
        // Resolve copy
        harness.passBothPriorities();
        // Resolve original
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Copy should not be in any graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Counsel of the Soratami") && c != counsel);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Counsel of the Soratami"));
    }

    @Test
    @DisplayName("Twincast itself goes to caster's graveyard after resolving")
    void twincastGoesToCasterGraveyard() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());

        // Resolve Twincast
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Twincast"));
    }

    @Test
    @DisplayName("Original spell goes to its owner's graveyard after resolving")
    void originalSpellGoesToOwnerGraveyard() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());

        // Resolve Twincast
        harness.passBothPriorities();
        // Resolve copy
        harness.passBothPriorities();
        // Resolve original
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c == counsel);
    }

    // ===== Copying targeted spells =====

    @Test
    @DisplayName("Copying a targeted instant preserves the target")
    void copyingTargetedInstantPreservesTarget() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());

        // Resolve Twincast
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        StackEntry copyEntry = gd.stack.getLast();
        assertThat(copyEntry.getDescription()).isEqualTo("Copy of Boomerang");
        assertThat(copyEntry.getTargetPermanentId()).isEqualTo(bearsPermId);
    }

    @Test
    @DisplayName("Copy resolves with same target — bounce effect applied twice")
    void copyAndOriginalBothBounce() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());

        // Resolve Twincast → creates copy, may-ability prompt for retarget
        harness.passBothPriorities();
        // Decline retarget — keep original target
        harness.handleMayAbilityChosen(player2, false);
        // Resolve copy → bounces Grizzly Bears
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Bears should be back in player1's hand after the copy's bounce
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Original Boomerang should fizzle since target is gone
        harness.passBothPriorities();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Twincast fizzles if target spell is countered before it resolves")
    void fizzlesIfTargetSpellCountered() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());

        // Remove target spell from stack to simulate it being countered
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Counsel of the Soratami"));

        harness.passBothPriorities();

        // Twincast fizzles
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        // Twincast still goes to graveyard when fizzling
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Twincast"));
    }

    // ===== Copy identity =====

    @Test
    @DisplayName("Copy has a different card identity than the original spell")
    void copyHasDifferentIdentity() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());

        // Resolve Twincast
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);

        StackEntry original = gd.stack.getFirst();
        StackEntry copy = gd.stack.getLast();

        assertThat(copy.getCard().getId()).isNotEqualTo(original.getCard().getId());
        assertThat(copy.getCard().getName()).isEqualTo(original.getCard().getName());
    }

    // ===== Copying your own spell =====

    @Test
    @DisplayName("Player can Twincast their own spell")
    void canCopyOwnSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Twincast twincast = new Twincast();
        harness.setHand(player1, List.of(counsel, twincast));
        harness.addMana(player1, ManaColor.BLUE, 5);

        GameData gd = harness.getGameData();
        int p1HandBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, 0);
        // After casting counsel, twincast is now at index 0
        harness.castInstant(player1, 0, counsel.getId());

        // Resolve Twincast → creates copy
        harness.passBothPriorities();
        // Resolve copy → player1 draws 2
        harness.passBothPriorities();
        // Resolve original → player1 draws 2
        harness.passBothPriorities();

        // Player1 cast 2 cards (hand -2), then drew 4 total (hand +4) = net +2
        int p1HandAfter = gd.playerHands.get(player1.getId()).size();
        assertThat(p1HandAfter - p1HandBefore).isEqualTo(2);
    }

    // ===== Game log =====

    @Test
    @DisplayName("Game log records that a copy was created")
    void gameLogRecordsCopyCreation() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());

        // Resolve Twincast
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("copy") && log.contains("Counsel of the Soratami"));
    }

    @Test
    @DisplayName("Game log records copy resolving")
    void gameLogRecordsCopyResolving() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());

        // Resolve Twincast
        harness.passBothPriorities();
        // Resolve copy
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Copy of Counsel of the Soratami") && log.contains("resolves"));
    }

    // ===== Stack is empty after everything resolves =====

    @Test
    @DisplayName("Stack is empty after Twincast, copy, and original all resolve")
    void stackEmptyAfterFullResolution() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());

        // Resolve Twincast
        harness.passBothPriorities();
        // Resolve copy
        harness.passBothPriorities();
        // Resolve original
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Copying an instant =====

    @Test
    @DisplayName("Twincast can copy an instant spell")
    void canCopyInstantSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);
        UUID bearsPermId = harness.getPermanentId(player2, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());

        // Resolve Twincast
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        StackEntry copyEntry = gd.stack.getLast();
        assertThat(copyEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(copyEntry.getDescription()).isEqualTo("Copy of Boomerang");
    }

    // ===== Copy retarget — "You may choose new targets for the copy" =====

    @Test
    @DisplayName("Copying a targeted spell offers retarget may-ability prompt")
    void copyOfTargetedSpellOffersRetarget() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());

        // Resolve Twincast → copy created, may-ability prompt for retarget
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Accepting retarget and choosing new target redirects the copy")
    void acceptRetargetChangesTarget() {
        GrizzlyBears bears1 = new GrizzlyBears();
        GrizzlyBears bears2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bears1);
        harness.addToBattlefield(player2, bears2);
        UUID bears1PermId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID bears2PermId = harness.getPermanentId(player2, "Grizzly Bears");

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        // Player1 casts Boomerang targeting their own bears
        harness.castInstant(player1, 0, bears1PermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());

        // Resolve Twincast → copy created
        harness.passBothPriorities();
        // Accept retarget
        harness.handleMayAbilityChosen(player2, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose player2's bears as new target
        harness.handlePermanentChosen(player2, bears2PermId);

        // Verify the copy's target changed
        StackEntry copyEntry = gd.stack.getLast();
        assertThat(copyEntry.getDescription()).isEqualTo("Copy of Boomerang");
        assertThat(copyEntry.getTargetPermanentId()).isEqualTo(bears2PermId);

        // Resolve copy → bounces player2's bears
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Player1's bears should still be on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Untargeted spell copy does not offer retarget prompt")
    void copyOfUntargetedSpellSkipsRetarget() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, counsel.getId());

        // Resolve Twincast → copy created, no retarget prompt
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Should NOT be awaiting may-ability choice
        assertThat(gd.interaction.awaitingInput).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        // Copy should be on the stack
        assertThat(gd.stack).anySatisfy(se ->
                assertThat(se.getDescription()).isEqualTo("Copy of Counsel of the Soratami"));
    }

    @Test
    @DisplayName("Retarget of creature-targeting spell only offers creatures, not other permanents")
    void retargetCreatureSpellOnlyOffersCreatures() {
        // Creature + enchantment on battlefield
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        GloriousAnthem anthem = new GloriousAnthem();
        harness.addToBattlefield(player1, anthem);
        UUID anthemPermId = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Glorious Anthem"))
                .findFirst().orElseThrow().getId();

        // Player1 casts Might of Oaks targeting bears
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, might.getId());

        // Resolve Twincast → accept retarget
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        // Valid targets should include the creature but NOT the enchantment
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds).contains(bearsPermId);
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds).doesNotContain(anthemPermId);
    }

    @Test
    @DisplayName("Retarget of 'any target' spell includes both creatures and players")
    void retargetAnyTargetSpellIncludesPlayers() {
        // Creature on battlefield
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        // Player1 casts Bandage targeting bears
        Bandage bandage = new Bandage();
        harness.setHand(player1, List.of(bandage));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bearsPermId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bandage.getId());

        // Resolve Twincast → accept retarget
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        // Valid targets should include the creature AND both players
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds).contains(bearsPermId);
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds).contains(player1.getId());
        assertThat(gd.interaction.awaitingPermanentChoiceValidIds).contains(player2.getId());
    }
}

