package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToOwnTappedCreaturesEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdeptWatershaperTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Adept Watershaper has correct card properties")
    void hasCorrectProperties() {
        AdeptWatershaper card = new AdeptWatershaper();

        assertThat(card.getName()).isEqualTo("Adept Watershaper");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{2}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(3);
        assertThat(card.getToughness()).isEqualTo(4);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.MERFOLK, CardSubtype.CLERIC);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(GrantKeywordToOwnTappedCreaturesEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Adept Watershaper puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new AdeptWatershaper()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Adept Watershaper");
    }

    @Test
    @DisplayName("Resolving puts Adept Watershaper onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new AdeptWatershaper()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Adept Watershaper"));
    }

    @Test
    @DisplayName("Adept Watershaper enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new AdeptWatershaper()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Adept Watershaper");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Static effect: grants indestructible to own tapped creatures =====

    @Test
    @DisplayName("Tapped creature you control gains indestructible")
    void tappedCreatureGainsIndestructible() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Untapped creature you control does not gain indestructible")
    void untappedCreatureDoesNotGainIndestructible() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(bears.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Adept Watershaper does not grant indestructible to itself")
    void doesNotGrantIndestructibleToItself() {
        harness.addToBattlefield(player1, new AdeptWatershaper());

        Permanent watershaper = findPermanent(player1, "Adept Watershaper");
        watershaper.tap();

        assertThat(gqs.hasKeyword(gd, watershaper, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant indestructible to opponent's tapped creatures")
    void doesNotGrantIndestructibleToOpponentCreatures() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");
        opponentBears.tap();

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Creature loses indestructible when it untaps")
    void creatureLosesIndestructibleWhenUntapped() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        bears.untap();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Indestructible is removed when Adept Watershaper leaves the battlefield")
    void indestructibleRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        // Remove Watershaper from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Adept Watershaper"));

        // Indestructible should be gone immediately (computed on the fly)
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    // ===== Static bonus survives end-of-turn reset =====

    @Test
    @DisplayName("Static indestructible survives end-of-turn modifier reset")
    void staticIndestructibleSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();

        // Simulate end-of-turn cleanup
        bears.resetModifiers();

        // Static keyword should still be computed
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    // ===== Multiple Watershapers =====

    @Test
    @DisplayName("Two Watershapers grant indestructible to each other when tapped")
    void twoWatershapersGrantIndestructibleToEachOther() {
        harness.addToBattlefield(player1, new AdeptWatershaper());
        harness.addToBattlefield(player1, new AdeptWatershaper());

        List<Permanent> watershapers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Adept Watershaper"))
                .toList();

        assertThat(watershapers).hasSize(2);

        // Tap both
        watershapers.get(0).tap();
        watershapers.get(1).tap();

        // Each should be indestructible from the other
        for (Permanent ws : watershapers) {
            assertThat(gqs.hasKeyword(gd, ws, Keyword.INDESTRUCTIBLE)).isTrue();
        }
    }

    // ===== Indestructible prevents "destroy" effects =====

    @Test
    @DisplayName("Indestructible tapped creature survives targeted destroy effect")
    void indestructibleSurvivesTargetedDestroy() {
        harness.addToBattlefield(player1, new AdeptWatershaper());

        Permanent tappedBears = new Permanent(new GrizzlyBears());
        tappedBears.tap();
        gd.playerBattlefields.get(player1.getId()).add(tappedBears);

        // Verify indestructible
        assertThat(gqs.hasKeyword(gd, tappedBears, Keyword.INDESTRUCTIBLE)).isTrue();

        // Cast Assassinate targeting the tapped creature
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, tappedBears.getId());
        harness.passBothPriorities();

        // Creature should survive — still on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Log should indicate indestructible
        assertThat(gd.gameLog).anyMatch(log -> log.contains("indestructible"));
    }

    @Test
    @DisplayName("Indestructible tapped creature survives Wrath of God")
    void indestructibleSurvivesWrathOfGod() {
        harness.addToBattlefield(player1, new AdeptWatershaper());

        Permanent tappedBears = new Permanent(new GrizzlyBears());
        tappedBears.tap();
        gd.playerBattlefields.get(player1.getId()).add(tappedBears);

        // Opponent has a creature too
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Verify indestructible
        assertThat(gqs.hasKeyword(gd, tappedBears, Keyword.INDESTRUCTIBLE)).isTrue();

        // Cast Wrath of God
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        // The tapped bears should survive (indestructible)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Watershaper was untapped → NOT indestructible → should be destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Adept Watershaper"));

        // Opponent's bears should be destroyed (not protected)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Tapped creature without Watershaper is NOT protected from destroy effects")
    void tappedCreatureWithoutWatershaperNotProtected() {
        // No Watershaper on battlefield — tapped creature has no indestructible
        Permanent tappedBears = new Permanent(new GrizzlyBears());
        tappedBears.tap();
        gd.playerBattlefields.get(player1.getId()).add(tappedBears);

        assertThat(gqs.hasKeyword(gd, tappedBears, Keyword.INDESTRUCTIBLE)).isFalse();

        // Cast Assassinate
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, tappedBears.getId());
        harness.passBothPriorities();

        // Creature should be destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Indestructible prevents lethal combat damage death =====

    @Test
    @DisplayName("Tapped attacker with indestructible survives lethal combat damage")
    void indestructibleAttackerSurvivesCombatDamage() {
        harness.addToBattlefield(player1, new AdeptWatershaper());

        // Small creature that will take lethal damage
        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        Permanent attacker = new Permanent(smallCreature);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.tap(); // Attackers are tapped → indestructible from Watershaper
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // Verify indestructible
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();

        // Big blocker that would normally kill the 1/1
        GrizzlyBears bigBlocker = new GrizzlyBears();
        bigBlocker.setPower(5);
        bigBlocker.setToughness(5);
        Permanent blocker = new Permanent(bigBlocker);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1); // Index 1 (Watershaper is 0, attacker is 1)
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage
        harness.passBothPriorities();

        // Attacker should survive (indestructible)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Blocker should survive too (5/5, took only 1 damage)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Indestructible blocker survives lethal combat damage from attacker")
    void indestructibleBlockerSurvivesCombatDamage() {
        harness.addToBattlefield(player2, new AdeptWatershaper());

        // Small blocker that will take lethal damage
        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        Permanent blocker = new Permanent(smallCreature);
        blocker.setSummoningSick(false);
        blocker.tap(); // Tapped → indestructible from Watershaper
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        // Verify indestructible
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.INDESTRUCTIBLE)).isTrue();

        // Set up blocking state
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        // Big attacker
        GrizzlyBears bigAttacker = new GrizzlyBears();
        bigAttacker.setPower(5);
        bigAttacker.setToughness(5);
        Permanent attacker = new Permanent(bigAttacker);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage
        harness.passBothPriorities();

        // Blocker should survive (indestructible)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Attacking creatures get tapped and thus become indestructible =====

    @Test
    @DisplayName("Attacking creatures are tapped and thus gain indestructible from Watershaper")
    void attackingCreatureGainsIndestructibleFromTapping() {
        harness.addToBattlefield(player1, new AdeptWatershaper());

        GrizzlyBears creature = new GrizzlyBears();
        Permanent attacker = new Permanent(creature);
        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // Before attacking — untapped, not indestructible
        assertThat(attacker.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isFalse();

        // Declare attackers
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(1)); // Index 1 (Watershaper at 0)

        // After declaring attackers — tapped and indestructible
        assertThat(attacker.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    // ===== Bonus applied on resolve =====

    @Test
    @DisplayName("Indestructible applies when Adept Watershaper resolves onto battlefield")
    void indestructibleAppliesOnResolve() {
        // Existing tapped creature
        Permanent tappedBears = new Permanent(new GrizzlyBears());
        tappedBears.tap();
        gd.playerBattlefields.get(player1.getId()).add(tappedBears);

        // Before Watershaper — no indestructible
        assertThat(gqs.hasKeyword(gd, tappedBears, Keyword.INDESTRUCTIBLE)).isFalse();

        // Cast and resolve Watershaper
        harness.setHand(player1, List.of(new AdeptWatershaper()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // After resolving, tapped creature should be indestructible
        assertThat(gqs.hasKeyword(gd, tappedBears, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    // ===== Helper methods =====

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
