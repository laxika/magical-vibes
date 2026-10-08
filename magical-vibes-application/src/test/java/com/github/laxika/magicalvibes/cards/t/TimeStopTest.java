package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BoonSatyr;
import com.github.laxika.magicalvibes.cards.e.EsikaGodOfTheTree;
import com.github.laxika.magicalvibes.cards.i.InvasionOfTolvada;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimeStop.class, ScatheZombies.class, InvasionOfTolvada.class, AirElemental.class,
        TheBrokenSky.class, EsikaGodOfTheTree.class, ThePrismaticBridge.class, BoonSatyr.class,
        RoyalAssassin.class})
class TimeStopTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts Time Stop on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    // ===== Basic resolution =====

    @Test
    @DisplayName("Resolving with empty stack exiles Time Stop and ends the turn")
    void resolvingEndsTheTurn() {
        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID activePlayerBefore = gd.activePlayerId;
        int turnBefore = gd.turnNumber;

        harness.castAndResolveInstant(player1, 0);

        // Time Stop is exiled, not in graveyard
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Time Stop"));
        harness.assertNotInGraveyard(player1, "Time Stop");

        // Turn advanced to next player
        assertThat(gd.activePlayerId).isNotEqualTo(activePlayerBefore);
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.stack).isEmpty();
    }

    // ===== Exiling spells from the stack =====

    @Test
    @DisplayName("Resolving exiles other spells on the stack")
    void exilesOtherSpellsOnStack() {
        // Player2 casts a creature, then player1 responds with Time Stop
        ScatheZombies bears = new ScatheZombies();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        // Scathe Zombies is exiled (not on battlefield, not in graveyard)
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Scathe Zombies"));
        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        harness.assertNotInGraveyard(player2, "Scathe Zombies");

        // Time Stop itself is exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Time Stop"));
    }

    @Test
    @DisplayName("Resolving exiles other spells into their owners' exile zones")
    void exilesOtherSpellsIntoTheirOwnersExileZones() {
        ScatheZombies bears = new ScatheZombies();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.setHand(player2, List.of(new TimeStop()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        gd.stack.stream()
                .filter(entry -> entry.getCard().getId().equals(bears.getId()))
                .findFirst()
                .orElseThrow()
                .setOwnerIdOverride(player2.getId());

        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Resolving exiles multiple spells on the stack")
    void exilesMultipleSpellsOnStack() {
        // Player2 casts a creature, lets it resolve, casts another creature,
        // then player1 responds with Time Stop
        ScatheZombies bears1 = new ScatheZombies();
        AirElemental angel = new AirElemental();
        harness.setHand(player2, List.of(bears1, angel));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast bears, then angel
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        // Bears resolves, now cast angel
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        // Player1 responds with Time Stop
        harness.castAndResolveInstant(player1, 0);

        // Air Elemental is exiled from the stack
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Air Elemental"));

        // Air Elemental is NOT on the battlefield
        harness.assertNotOnBattlefield(player2, "Air Elemental");

        // Scathe Zombies resolved earlier and is on the battlefield
        harness.assertOnBattlefield(player2, "Scathe Zombies");
    }

    @Test
    @DisplayName("Resolving exiles Battle spells on the stack")
    void exilesBattleSpellOnStack() {
        InvasionOfTolvada invasion = new InvasionOfTolvada();
        harness.setHand(player2, List.of(invasion));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Choose player1 as the opponent protecting the Siege, then respond before it resolves.
        gs.playCard(gd, player2, 0, 0, player1.getId(), null);
        harness.passPriority(player2);
        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Invasion of Tolvada"));
        harness.assertNotOnBattlefield(player2, "Invasion of Tolvada");
        harness.assertNotInGraveyard(player2, "Invasion of Tolvada");
    }

    @Test
    @DisplayName("Resolving exiles modal double-faced spells as their physical cards")
    void exilesModalDoubleFacedSpellsAsPhysicalCards() {
        EsikaGodOfTheTree esika = new EsikaGodOfTheTree();
        harness.setHand(player1, List.of(esika));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new TimeStop()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.playCard(gd, player1, 0, 1, null, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(esika.getId()));
    }

    @Test
    @DisplayName("Resolving exiles bestowed spells as their physical cards")
    void exilesBestowedSpellsAsPhysicalCards() {
        Permanent bears = addCreatureReady(player1, new ScatheZombies());
        BoonSatyr boonSatyr = new BoonSatyr();
        harness.setHand(player1, List.of(boonSatyr));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.setHand(player2, List.of(new TimeStop()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, bears.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card == boonSatyr);
    }

    @Test
    @DisplayName("Resolving does not put spell copies into exile")
    void doesNotExileSpellCopies() {
        ScatheZombies copiedBears = new ScatheZombies();
        StackEntry copiedSpell = new StackEntry(StackEntryType.CREATURE_SPELL, copiedBears,
                player2.getId(), "Copy of Scathe Zombies", List.of());
        copiedSpell.setCopy(true);
        gd.stack.add(copiedSpell);

        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(copiedBears.getId()));
    }

    // ===== Combat state =====

    @Test
    @DisplayName("Resolving during combat clears combat state")
    void clearsCombatStateDuringCombat() {
        Permanent attackingBears = harness.addToBattlefieldAndReturn(player1, new ScatheZombies());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        // Simulate a creature that is attacking
        attackingBears.setAttacking(true);

        harness.setHand(player2, List.of(new TimeStop()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        // Combat state is cleared — creature should no longer be attacking
        assertThat(attackingBears.isAttacking()).isFalse();
    }

    // ===== End-of-turn modifiers =====

    @Test
    @DisplayName("Resolving resets end-of-turn modifiers on permanents")
    void resetsEndOfTurnModifiers() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new ScatheZombies());
        perm.setPowerModifier(3);
        perm.setToughnessModifier(3);

        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(perm.getPowerModifier()).isZero();
        assertThat(perm.getToughnessModifier()).isZero();
    }

    // ===== Pending may abilities =====

    @Test
    @DisplayName("Resolving clears pending may abilities")
    void clearsPendingMayAbilities() {
        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0);

        // Simulate a pending may ability that exists when Time Stop resolves
        gd.pendingMayAbilities.add(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                new ScatheZombies(), player1.getId(), List.of(), "Do something?"
        ));

        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    // ===== Game log =====

    @Test
    @DisplayName("Game log records that the turn ends")
    void gameLogRecordsTurnEnds() {
        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gameLogContains("The turn ends")).isTrue();
    }

    @Test
    @DisplayName("Game log records exiled spells")
    void gameLogRecordsExiledSpells() {
        ScatheZombies bears = new ScatheZombies();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gameLogContains("Scathe Zombies is exiled")).isTrue();
    }

    // ===== End-of-combat sacrifices =====

    @Test
    @DisplayName("Skipping end of combat preserves its untriggered delayed sacrifice")
    void preservesUntriggeredEndOfCombatSacrifice() {
        Permanent creature = addCreatureReady(player1, new ScatheZombies());
        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        SacrificeAtEndOfCombat sacrifice = new SacrificeAtEndOfCombat(creature.getId());
        gd.queueDelayedAction(sacrifice);

        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Scathe Zombies");
        assertThat(gd.getDelayedActions(SacrificeAtEndOfCombat.class)).contains(sacrifice);
    }

    @Test
    @DisplayName("Resolving processes permanents scheduled for the next cleanup")
    void processesPermanentsScheduledForNextCleanup() {
        Permanent bears = addCreatureReady(player1, new ScatheZombies());
        bears.setSacrificeAtNextCleanup(true);

        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotOnBattlefield(player1, "Scathe Zombies");
        harness.assertInGraveyard(player1, "Scathe Zombies");
    }

    @Test
    @DisplayName("Resolving makes the active player discard down to maximum hand size")
    void discardsActivePlayerDownToMaximumHandSize() {
        harness.setHand(player1, List.of(
                new ScatheZombies(), new ScatheZombies(), new ScatheZombies(), new ScatheZombies(),
                new ScatheZombies(), new ScatheZombies(), new ScatheZombies(), new ScatheZombies()
        ));
        harness.setHand(player2, List.of(new TimeStop()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player1);
        harness.castInstant(player2, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        harness.assertInGraveyard(player1, "Scathe Zombies");
    }

    @Test
    @DisplayName("Resolving removes damage marked on permanents")
    void removesMarkedDamage() {
        Permanent bears = addCreatureReady(player1, new ScatheZombies());
        bears.setMarkedDamage(1);

        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveInstant(player1, 0);

        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Resolving checks lethal damage before cleanup removes it")
    void checksStateBasedActionsBeforeRemovingLethalDamage() {
        harness.setHand(player1, List.of(new TimeStop()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castInstant(player1, 0);

        // Add lethal damage after the cast-time state-based-action check, while Time Stop is on the stack.
        Permanent bears = addCreatureReady(player1, new ScatheZombies());
        bears.setMarkedDamage(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scathe Zombies");
        harness.assertInGraveyard(player1, "Scathe Zombies");
    }

    @Test
    @DisplayName("Time Stop removes activated abilities without removing their sources or targets")
    void removesActivatedAbilitiesFromStack() {
        addCreatureReady(player1, new RoyalAssassin());
        Permanent target = addCreatureReady(player2, new ScatheZombies());
        target.tap();
        harness.setHand(player2, List.of(new TimeStop()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Royal Assassin");
        harness.assertOnBattlefield(player2, "Scathe Zombies");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Damage and temporary modifiers remain until cleanup discards finish")
    void cleanupDiscardsBeforeRemovingDamageAndModifiers() {
        Permanent creature = addCreatureReady(player1, new ScatheZombies());
        creature.setMarkedDamage(1);
        creature.setPowerModifier(3);
        creature.setToughnessModifier(3);
        harness.setHand(player1, List.of(
                new ScatheZombies(), new ScatheZombies(), new ScatheZombies(), new ScatheZombies(),
                new ScatheZombies(), new ScatheZombies(), new ScatheZombies(), new ScatheZombies()));
        harness.setHand(player2, List.of(new TimeStop()));
        harness.addMana(player2, ManaColor.BLUE, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);

        harness.handleCardChosen(player1, 0);

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }
}

