package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.a.AngelicBlessing;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.AwakenerDruid;
import com.github.laxika.magicalvibes.cards.a.AvenCloudchaser;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.i.InfernalPlunge;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MakeshiftMauler;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SkaabRuinator;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.u.UnburialRites;
import com.github.laxika.magicalvibes.cards.v.Vivisection;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AiDecisionEngineTest {

    private GameTestHarness harness;
    private Player human;
    private Player aiPlayer;
    private GameData gd;
    private EasyAiDecisionEngine ai;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        human = harness.getPlayer1();
        aiPlayer = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();

        FakeConnection aiConn = new FakeConnection("ai-test");
        harness.getSessionManager().registerPlayer(aiConn, aiPlayer.getId(), "Bob");
        ai = new EasyAiDecisionEngine(gd.id, aiPlayer, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService());
        ai.setSelfConnection(aiConn);
    }

    /**
     * Sets up the game so the AI (player2) has priority in precombat main with an empty stack.
     */
    private void giveAiPriority() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
    }

    /**
     * Adds the given number of untapped Plains to the AI's battlefield.
     */
    private void giveAiPlains(int count) {
        for (int i = 0; i < count; i++) {
            Permanent plains = new Permanent(new Plains());
            plains.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(plains);
        }
    }

    /**
     * Adds the given number of untapped Mountains to the AI's battlefield.
     */
    private void giveAiMountains(int count) {
        for (int i = 0; i < count; i++) {
            Permanent mountain = new Permanent(new Mountain());
            mountain.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(mountain);
        }
    }

    // ===== Sacrifice cost checks =====

    @Test
    @DisplayName("AI skips spell with SacrificeArtifactCost when no artifact on battlefield")
    void skipsSpellWithSacrificeArtifactCostWhenNoArtifact() {
        giveAiPriority();
        giveAiMountains(1);

        harness.setHand(aiPlayer, List.of(new KuldothaRebirth()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no artifact to sacrifice
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI skips spell with SacrificeCreatureCost when no creature on battlefield")
    void skipsSpellWithSacrificeCreatureCostWhenNoCreature() {
        giveAiPriority();
        giveAiMountains(1);

        harness.setHand(aiPlayer, List.of(new InfernalPlunge()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no creature to sacrifice
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI skips sacrifice-cost spell but casts another available spell")
    void castsOtherSpellWhenSacrificeCostCannotBePaid() {
        giveAiPriority();

        // Mountain + Forest so both KuldothaRebirth ({R}) and GrizzlyBears ({1}{G}) are affordable
        Permanent mountain = new Permanent(new Mountain());
        mountain.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(mountain);

        Permanent forest = new Permanent(new Forest());
        forest.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(forest);

        harness.setHand(aiPlayer, List.of(new KuldothaRebirth(), new GrizzlyBears()));

        ai.handleMessage("GAME_STATE", "");

        // Should skip KuldothaRebirth (no artifact) and cast GrizzlyBears
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== selectSacrificeTarget =====

    @Test
    @DisplayName("selectSacrificeTarget picks weakest creature for SacrificeCreatureCost")
    void selectSacrificeTargetPicksWeakestCreature() {
        Permanent elves = new Permanent(new LlanowarElves()); // 1/1
        Permanent bears = new Permanent(new GrizzlyBears()); // 2/2
        Permanent angel = new Permanent(new SerraAngel()); // 4/4
        gd.playerBattlefields.get(aiPlayer.getId()).addAll(List.of(elves, bears, angel));

        var result = ai.selectSacrificeTarget(gd, new Vivisection());

        assertThat(result).isEqualTo(elves.getId());
    }

    @Test
    @DisplayName("selectSacrificeTarget returns null for card with no sacrifice cost")
    void selectSacrificeTargetReturnsNullForNoCost() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears);

        var result = ai.selectSacrificeTarget(gd, new GrizzlyBears());

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("selectSacrificeTarget returns null when no creature available")
    void selectSacrificeTargetReturnsNullWhenNoCreature() {
        var result = ai.selectSacrificeTarget(gd, new Vivisection());

        assertThat(result).isNull();
    }

    // ===== AI casts sacrifice-cost spell end-to-end =====

    @Test
    @DisplayName("AI casts Vivisection by sacrificing weakest creature")
    void castsVivisectionSacrificingWeakestCreature() {
        giveAiPriority();

        // Give AI Islands for Vivisection's {3}{U} cost
        for (int i = 0; i < 4; i++) {
            Permanent island = new Permanent(new com.github.laxika.magicalvibes.cards.i.Island());
            island.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(island);
        }

        Permanent elves = new Permanent(new LlanowarElves()); // 1/1 — should be sacrificed
        elves.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(elves);

        Permanent angel = new Permanent(new SerraAngel()); // 4/4 — should survive
        angel.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(angel);

        harness.setHand(aiPlayer, List.of(new Vivisection()));

        ai.handleMessage("GAME_STATE", "");

        // Vivisection should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vivisection");
        // Llanowar Elves should have been sacrificed (weakest creature)
        harness.assertNotOnBattlefield(aiPlayer, "Llanowar Elves");
        // Serra Angel should still be on the battlefield
        harness.assertOnBattlefield(aiPlayer, "Serra Angel");
    }

    // ===== Detrimental aura targeting =====

    @Test
    @DisplayName("AI casts Pacifism on opponent's highest-power creature")
    void castsPacifismOnHighestPowerCreature() {
        giveAiPriority();
        giveAiPlains(2);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pacifism");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(airElemental.getId());
    }

    @Test
    @DisplayName("AI does not cast second Pacifism on already-pacified creature")
    void doesNotDoublePacify() {
        giveAiPriority();
        giveAiPlains(2);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        // Air Elemental already has Pacifism attached
        Permanent existingPacifism = new Permanent(new Pacifism());
        existingPacifism.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(aiPlayer.getId()).add(existingPacifism);

        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        // AI should cast on the Grizzly Bears instead
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pacifism");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("AI does not cast Pacifism when all opponent creatures already have one")
    void doesNotCastWhenAllCreaturesPacified() {
        giveAiPriority();
        giveAiPlains(2);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        // Bears already has Pacifism
        Permanent existingPacifism = new Permanent(new Pacifism());
        existingPacifism.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(aiPlayer.getId()).add(existingPacifism);

        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no valid targets, so it passes priority instead
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI casts Pacifism when opponent has mix of pacified and unpacified creatures")
    void castsOnUnpacifiedAmongMixed() {
        giveAiPriority();
        giveAiPlains(2);

        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears1);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears2);

        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        // Air Elemental already pacified
        Permanent existingPacifism = new Permanent(new Pacifism());
        existingPacifism.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(aiPlayer.getId()).add(existingPacifism);

        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        // Should target one of the unpacified bears (both 2/2, either is valid)
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId())
                .isIn(bears1.getId(), bears2.getId());
    }

    // ===== Beneficial aura targeting =====

    @Test
    @DisplayName("AI casts beneficial aura on own creature")
    void castsBeneficialAuraOnOwnCreature() {
        giveAiPriority();

        // Holy Strength costs {W}
        Permanent plains = new Permanent(new Plains());
        plains.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(plains);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new HolyStrength()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Holy Strength");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
    }

    // ===== No target available =====

    @Test
    @DisplayName("AI does not cast Pacifism when opponent has no creatures")
    void doesNotCastWithoutOpponentCreatures() {
        giveAiPriority();
        giveAiPlains(2);

        // Opponent has no creatures
        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).isEmpty();
    }

    // ===== Target filter fallback =====

    @Test
    @DisplayName("AI casts Awakener Druid targeting own Forest")
    void castsAwakenerDruidTargetingForest() {
        giveAiPriority();

        // Awakener Druid costs {2}{G} — give AI 3 Forests (one will be the target)
        for (int i = 0; i < 3; i++) {
            Permanent forest = new Permanent(new Forest());
            forest.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(forest);
        }

        harness.setHand(aiPlayer, List.of(new AwakenerDruid()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Awakener Druid");
        // Target must be one of the AI's Forests
        UUID targetId = gd.stack.getFirst().getTargetId();
        assertThat(gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst()
                .orElseThrow()
                .getCard().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("AI does not cast Awakener Druid when no Forests on battlefield")
    void doesNotCastAwakenerDruidWithoutForests() {
        giveAiPriority();

        // Give AI enough mana for {2}{G} via Swamps + floating green, but no Forest permanents
        for (int i = 0; i < 2; i++) {
            Permanent swamp = new Permanent(new Swamp());
            swamp.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(swamp);
        }
        harness.addMana(aiPlayer, ManaColor.GREEN, 1);

        harness.setHand(aiPlayer, List.of(new AwakenerDruid()));

        ai.handleMessage("GAME_STATE", "");

        // No valid Forest target — AI should pass priority instead
        assertThat(gd.stack).isEmpty();
    }

    // ===== Creature mana restriction =====

    @Test
    @DisplayName("AI does not cast Myr Superion with only land mana")
    void doesNotCastMyrSuperionWithLandMana() {
        giveAiPriority();
        giveAiPlains(2);

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.m.MyrSuperion()));

        ai.handleMessage("GAME_STATE", "");

        // Myr Superion should NOT be on the stack — only land mana is available
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI casts Myr Superion when creature mana dorks are available")
    void castsMyrSuperionWithCreatureMana() {
        giveAiPriority();

        // Add two Llanowar Elves (creature mana dorks)
        Permanent elf1 = new Permanent(new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        elf1.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(elf1);

        Permanent elf2 = new Permanent(new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        elf2.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(elf2);

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.m.MyrSuperion()));

        ai.handleMessage("GAME_STATE", "");

        // Myr Superion should be on the stack — creature mana from elves
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Myr Superion");
    }

    @Test
    @DisplayName("AI skips Myr Superion but casts normal spell when only land mana available")
    void skipsMyrSuperionButCastsNormalSpell() {
        giveAiPriority();

        // Use Forests so GrizzlyBears ({1}{G}) is castable
        for (int i = 0; i < 2; i++) {
            Permanent forest = new Permanent(new Forest());
            forest.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(forest);
        }

        harness.setHand(aiPlayer, List.of(
                new com.github.laxika.magicalvibes.cards.m.MyrSuperion(),
                new GrizzlyBears()));

        ai.handleMessage("GAME_STATE", "");

        // Should skip Myr Superion and cast the GrizzlyBears instead
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Cost modifier (opponent tax effects) =====

    @Test
    @DisplayName("AI taps enough lands for spell when opponent has cost-increasing effect")
    void tapsEnoughLandsWithCostIncreasingEffect() {
        giveAiPriority();

        // Holy Strength is an enchantment aura costing {W}.
        // AuraOfSilence increases opponent's artifact/enchantment costs by 2,
        // so the effective cost becomes {2}{W} = 3 mana.
        giveAiPlains(3);

        // Put AuraOfSilence on human's battlefield to tax AI's enchantment spells
        Permanent auraOfSilence = new Permanent(new AuraOfSilence());
        auraOfSilence.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(auraOfSilence);

        // AI needs a creature to target with the aura
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new HolyStrength()));

        ai.handleMessage("GAME_STATE", "");

        // AI should successfully cast despite the tax — it taps 3 Plains
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Holy Strength");
    }

    @Test
    @DisplayName("AI does not cast taxed spell when insufficient lands available")
    void doesNotCastTaxedSpellWithInsufficientLands() {
        giveAiPriority();

        // Only 1 Plains — enough for base cost {W} but not for taxed cost {2}{W}
        giveAiPlains(1);

        Permanent auraOfSilence = new Permanent(new AuraOfSilence());
        auraOfSilence.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(auraOfSilence);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new HolyStrength()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — can't afford {2}{W} with only 1 Plains
        assertThat(gd.stack).isEmpty();
    }

    // ===== Blocker declaration =====

    /**
     * Sets up the game so the human is the active player attacking, and the AI is defending.
     */
    private void setupBlockerPhase() {
        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }

    @Test
    @DisplayName("AI does not attempt to block an unblockable creature")
    void doesNotBlockUnblockableCreature() {
        setupBlockerPhase();

        // Human attacks with Phantom Warrior (unblockable)
        Permanent phantomWarrior = new Permanent(new PhantomWarrior());
        phantomWarrior.setSummoningSick(false);
        phantomWarrior.setAttacking(true);
        gd.playerBattlefields.get(human.getId()).add(phantomWarrior);

        // AI has a creature that could theoretically block
        Permanent aiBears = new Permanent(new GrizzlyBears());
        aiBears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(aiBears);

        // Should not throw — AI skips the unblockable attacker
        ai.handleMessage("AVAILABLE_BLOCKERS", "");

        // Bears should not be tapped (not assigned as blocker)
        assertThat(aiBears.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("AI blocks normal attacker but skips unblockable attacker")
    void blocksNormalButSkipsUnblockable() {
        setupBlockerPhase();

        // Human attacks with Phantom Warrior (unblockable) and Grizzly Bears (blockable)
        Permanent phantomWarrior = new Permanent(new PhantomWarrior());
        phantomWarrior.setSummoningSick(false);
        phantomWarrior.setAttacking(true);
        gd.playerBattlefields.get(human.getId()).add(phantomWarrior);

        Permanent humanBears = new Permanent(new GrizzlyBears());
        humanBears.setSummoningSick(false);
        humanBears.setAttacking(true);
        gd.playerBattlefields.get(human.getId()).add(humanBears);

        // AI has Air Elemental — big enough to favorably block Grizzly Bears (4/4 vs 2/2)
        Permanent aiElemental = new Permanent(new AirElemental());
        aiElemental.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(aiElemental);

        // Set life low enough that lethal incoming triggers chump-block logic too
        gd.playerLifeTotals.put(aiPlayer.getId(), 3);

        ai.handleMessage("AVAILABLE_BLOCKERS", "");
        harness.passBothPriorities();

        // Combat fully resolves — assert on outcomes:
        // Phantom Warrior was unblocked, so only its 2 damage got through (3 - 2 = 1)
        assertThat(gd.playerLifeTotals.get(aiPlayer.getId())).isEqualTo(1);
        // Grizzly Bears was blocked by Air Elemental and died
        assertThat(gd.playerBattlefields.get(human.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Air Elemental survived (4/4 vs 2/2)
        assertThat(gd.playerBattlefields.get(aiPlayer.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
    }

    // ===== Must-attack =====

    private void setupAttackerPhase() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
    }

    @Test
    @DisplayName("Easy AI includes must-attack creature in attack declaration")
    void includesMustAttackCreature() {
        setupAttackerPhase();

        // AI has Berserkers of Blood Ridge (4/4 must-attack)
        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(berserkers);

        // Opponent has Air Elemental (4/4 flying) — can block
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        ai.handleMessage("AVAILABLE_ATTACKERS", "");

        // Berserkers must be attacking despite the unfavorable board
        assertThat(berserkers.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Easy AI includes must-attack creature alongside optional creatures")
    void includesMustAttackWithOptional() {
        setupAttackerPhase();
        gd.playerLifeTotals.put(human.getId(), 20);

        // AI has Berserkers (4/4 must-attack) and Bears (2/2 optional)
        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(berserkers);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears);

        ai.handleMessage("AVAILABLE_ATTACKERS", "");

        // Berserkers (4 power) must have attacked; combat fully resolves with no blockers
        assertThat(gd.playerLifeTotals.get(human.getId())).isLessThanOrEqualTo(16);
    }

    // ===== Blocker fallback =====

    @Test
    @DisplayName("AI does not get stuck when blocker declaration is rejected")
    void blockerFallbackOnInvalidDeclaration() {
        setupBlockerPhase();

        // Human attacks with Grizzly Bears
        Permanent humanBears = new Permanent(new GrizzlyBears());
        humanBears.setSummoningSick(false);
        humanBears.setAttacking(true);
        gd.playerBattlefields.get(human.getId()).add(humanBears);

        // AI has Air Elemental to block with
        Permanent aiElemental = new Permanent(new AirElemental());
        aiElemental.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(aiElemental);

        ai.handleMessage("AVAILABLE_BLOCKERS", "");

        // The blocker declaration should have been accepted (no stuck state)
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    // ===== Creature-targeting spell validation =====

    @Test
    @DisplayName("AI casts Angelic Blessing targeting own creature, not a land")
    void castsAngelicBlessingTargetingCreatureNotLand() {
        giveAiPriority();
        giveAiPlains(3);

        // AI has a creature on the battlefield
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new AngelicBlessing()));

        ai.handleMessage("GAME_STATE", "");

        // AI should cast Angelic Blessing targeting the creature, not a Plains
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Angelic Blessing");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("AI does not cast Angelic Blessing when no creatures on battlefield")
    void doesNotCastAngelicBlessingWithoutCreatures() {
        giveAiPriority();
        giveAiPlains(3);

        // Only lands on battlefield, no creatures
        harness.setHand(aiPlayer, List.of(new AngelicBlessing()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no valid creature targets
        assertThat(gd.stack).isEmpty();
    }

    // ===== ETB destroy targeting =====

    @Test
    @DisplayName("AI casts Aven Cloudchaser targeting opponent's enchantment, not a creature")
    void castsAvenCloudchaserTargetingEnchantment() {
        giveAiPriority();
        giveAiPlains(4);

        // Opponent has a creature and an enchantment
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        Permanent chorus = new Permanent(new AngelicChorus());
        gd.playerBattlefields.get(human.getId()).add(chorus);

        harness.setHand(aiPlayer, List.of(new AvenCloudchaser()));

        ai.handleMessage("GAME_STATE", "");

        // AI should cast Aven Cloudchaser targeting the enchantment, not the creature
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Aven Cloudchaser");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(chorus.getId());
    }

    @Test
    @DisplayName("AI does not cast Aven Cloudchaser when no enchantments on battlefield")
    void doesNotCastAvenCloudchaserWithoutEnchantments() {
        giveAiPriority();
        giveAiPlains(4);

        // Opponent has only creatures, no enchantments
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new AvenCloudchaser()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no valid enchantment targets
        assertThat(gd.stack).isEmpty();
    }

    // ===== Graveyard targeting =====

    /**
     * Adds the given number of untapped Swamps to the AI's battlefield.
     */
    private void giveAiSwamps(int count) {
        for (int i = 0; i < count; i++) {
            Permanent swamp = new Permanent(new Swamp());
            swamp.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(swamp);
        }
    }

    @Test
    @DisplayName("AI casts Unburial Rites targeting highest-MV creature in own graveyard")
    void castsUnburialRitesTargetingHighestManaValueCreature() {
        giveAiPriority();
        giveAiSwamps(3);
        giveAiPlains(2);

        // Put two creatures in AI's graveyard — Grizzly Bears (MV 2) and Air Elemental (MV 5)
        Card bears = new GrizzlyBears();
        Card airElemental = new AirElemental();
        gd.playerGraveyards.get(aiPlayer.getId()).add(bears);
        gd.playerGraveyards.get(aiPlayer.getId()).add(airElemental);

        harness.setHand(aiPlayer, List.of(new UnburialRites()));

        ai.handleMessage("GAME_STATE", "");

        // AI should cast Unburial Rites targeting Air Elemental (highest MV)
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Unburial Rites");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(airElemental.getId());
    }

    @Test
    @DisplayName("AI does not cast Unburial Rites when no creature in graveyard")
    void doesNotCastUnburialRitesWithoutCreatureInGraveyard() {
        giveAiPriority();
        giveAiSwamps(3);
        giveAiPlains(2);

        // Graveyard has only a non-creature card
        Card holyDay = new HolyDay();
        gd.playerGraveyards.get(aiPlayer.getId()).add(holyDay);

        harness.setHand(aiPlayer, List.of(new UnburialRites()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no valid creature target
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI does not cast Unburial Rites when graveyard is empty")
    void doesNotCastUnburialRitesWithEmptyGraveyard() {
        giveAiPriority();
        giveAiSwamps(3);
        giveAiPlains(2);

        harness.setHand(aiPlayer, List.of(new UnburialRites()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no target in graveyard
        assertThat(gd.stack).isEmpty();
    }

    // ===== Graveyard creature requirement (ExileCreaturesFromGraveyardAndCreateTokensEffect) =====

    @Test
    @DisplayName("AI does not cast Midnight Ritual when graveyard has no creature cards")
    void doesNotCastMidnightRitualWithEmptyGraveyard() {
        giveAiPriority();
        giveAiSwamps(4); // Midnight Ritual costs {X}{2}{B}, X=1 needs 4 mana

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.m.MidnightRitual()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no creature cards in graveyard for X
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI does not cast Midnight Ritual when graveyard has only non-creature cards")
    void doesNotCastMidnightRitualWithOnlyNonCreaturesInGraveyard() {
        giveAiPriority();
        giveAiSwamps(4);

        // Graveyard has a non-creature card
        gd.playerGraveyards.get(aiPlayer.getId()).add(new com.github.laxika.magicalvibes.cards.h.HolyDay());

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.m.MidnightRitual()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no creature cards in graveyard
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI casts Midnight Ritual when graveyard has creature cards")
    void castsMidnightRitualWithCreatureInGraveyard() {
        giveAiPriority();
        giveAiSwamps(4); // Enough for X=1: {1}{2}{B}

        // Put a creature in the graveyard
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.m.MidnightRitual()));

        ai.handleMessage("GAME_STATE", "");

        // AI cast the spell — card was removed from hand, game awaits graveyard selection
        assertThat(gd.playerHands.get(aiPlayer.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // AI responds to the graveyard choice, putting the spell on the stack
        ai.handleMessage("CHOOSE_MULTIPLE_CARDS_FROM_GRAVEYARDS", "");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Midnight Ritual");
    }

    @Test
    @DisplayName("AI caps Midnight Ritual X at number of creatures in graveyard")
    void capsMidnightRitualXAtGraveyardCreatureCount() {
        giveAiPriority();
        giveAiSwamps(6); // Enough mana for X=3, but only 1 creature in graveyard

        // Put 1 creature in the graveyard
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.m.MidnightRitual()));

        ai.handleMessage("GAME_STATE", "");

        // AI cast the spell — card was removed from hand
        assertThat(gd.playerHands.get(aiPlayer.getId())).isEmpty();

        // AI responds to the graveyard choice
        ai.handleMessage("CHOOSE_MULTIPLE_CARDS_FROM_GRAVEYARDS", "");

        // AI should cast with X capped at 1 (graveyard creature count), not X=3 (max mana)
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Midnight Ritual");
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(1);
    }

    // ===== Graveyard exile cost (ExileNCardsFromGraveyardCost / ExileCardFromGraveyardCost) =====

    /**
     * Adds the given number of untapped Islands to the AI's battlefield.
     */
    private void giveAiIslands(int count) {
        for (int i = 0; i < count; i++) {
            Permanent island = new Permanent(new Island());
            island.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(island);
        }
    }

    @Test
    @DisplayName("AI does not cast Skaab Ruinator when graveyard has fewer than 3 creatures")
    void doesNotCastSkaabRuinatorWithTooFewCreaturesInGraveyard() {
        giveAiPriority();
        giveAiIslands(3); // Skaab Ruinator costs {1}{U}{U}

        // Put only 2 creatures in graveyard (need 3)
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());

        harness.setHand(aiPlayer, List.of(new SkaabRuinator()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — not enough creature cards in graveyard
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI does not cast Skaab Ruinator when graveyard has only non-creature cards")
    void doesNotCastSkaabRuinatorWithOnlyNonCreaturesInGraveyard() {
        giveAiPriority();
        giveAiIslands(3);

        // Graveyard has 3 non-creature cards
        gd.playerGraveyards.get(aiPlayer.getId()).add(new HolyDay());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new HolyDay());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new HolyDay());

        harness.setHand(aiPlayer, List.of(new SkaabRuinator()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no creature cards in graveyard
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI does not cast Skaab Ruinator when graveyard is empty")
    void doesNotCastSkaabRuinatorWithEmptyGraveyard() {
        giveAiPriority();
        giveAiIslands(3);

        harness.setHand(aiPlayer, List.of(new SkaabRuinator()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — empty graveyard
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI does not cast Makeshift Mauler when no creature card in graveyard")
    void doesNotCastMakeshiftMaulerWithNoCreatureInGraveyard() {
        giveAiPriority();
        giveAiIslands(5); // Makeshift Mauler costs {3}{U}

        // Graveyard has only a non-creature card
        gd.playerGraveyards.get(aiPlayer.getId()).add(new HolyDay());

        harness.setHand(aiPlayer, List.of(new MakeshiftMauler()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no creature card in graveyard to exile
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI does not cast Makeshift Mauler when graveyard is empty")
    void doesNotCastMakeshiftMaulerWithEmptyGraveyard() {
        giveAiPriority();
        giveAiIslands(5);

        harness.setHand(aiPlayer, List.of(new MakeshiftMauler()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — empty graveyard
        assertThat(gd.stack).isEmpty();
    }

    // ===== tryPlayLand silent failure recovery =====

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("tryPlayLand silent failure recovery")
    class TryPlayLandSilentFailureRecovery {

        @Mock private MessageHandler mockMessageHandler;
        @Mock private GameQueryService mockGameQueryService;
        @Mock private CombatAttackService mockCombatAttackService;
        @Mock private Connection mockConnection;
        @Mock private GameBroadcastService mockGameBroadcastService;
        @Mock private com.github.laxika.magicalvibes.service.effect.TargetValidationService mockTargetValidationService;

        private GameData mockGd;
        private Player mockAiPlayer;
        private GameRegistry mockGameRegistry;

        @BeforeEach
        void setUpMocks() {
            UUID gameId = UUID.randomUUID();
            mockAiPlayer = new Player(UUID.randomUUID(), "AI");
            Player mockOpponent = new Player(UUID.randomUUID(), "Opponent");

            mockGd = new GameData(gameId, "test", mockAiPlayer.getId(), "AI");
            mockGd.status = GameStatus.RUNNING;
            mockGd.currentStep = TurnStep.PRECOMBAT_MAIN;
            mockGd.activePlayerId = mockAiPlayer.getId();
            mockGd.orderedPlayerIds.add(mockAiPlayer.getId());
            mockGd.orderedPlayerIds.add(mockOpponent.getId());
            mockGd.playerIdToName.put(mockAiPlayer.getId(), "AI");
            mockGd.playerIdToName.put(mockOpponent.getId(), "Opponent");
            mockGd.playerHands.put(mockAiPlayer.getId(), Collections.synchronizedList(new ArrayList<>()));
            mockGd.playerHands.put(mockOpponent.getId(), Collections.synchronizedList(new ArrayList<>()));
            mockGd.playerBattlefields.put(mockAiPlayer.getId(), Collections.synchronizedList(new ArrayList<>()));
            mockGd.playerBattlefields.put(mockOpponent.getId(), Collections.synchronizedList(new ArrayList<>()));
            mockGd.playerManaPools.put(mockAiPlayer.getId(), new ManaPool());
            mockGd.playerManaPools.put(mockOpponent.getId(), new ManaPool());
            mockGd.playerLifeTotals.put(mockAiPlayer.getId(), 20);
            mockGd.playerLifeTotals.put(mockOpponent.getId(), 20);
            mockGd.playerDecks.put(mockAiPlayer.getId(), Collections.synchronizedList(new ArrayList<>()));
            mockGd.playerDecks.put(mockOpponent.getId(), Collections.synchronizedList(new ArrayList<>()));
            mockGd.playerGraveyards.put(mockAiPlayer.getId(), Collections.synchronizedList(new ArrayList<>()));
            mockGd.playerGraveyards.put(mockOpponent.getId(), Collections.synchronizedList(new ArrayList<>()));

            mockGameRegistry = new GameRegistry();
            mockGameRegistry.register(mockGd);
        }

        private EasyAiDecisionEngine createEngine() {
            EasyAiDecisionEngine engine = new EasyAiDecisionEngine(
                    mockGd.id, mockAiPlayer, mockGameRegistry, mockMessageHandler,
                    mockGameQueryService, mockCombatAttackService, mockGameBroadcastService,
                    mockTargetValidationService);
            engine.setSelfConnection(mockConnection);
            return engine;
        }

        @Test
        @DisplayName("AI passes priority when land play is silently rejected")
        void passesPriorityWhenLandPlaySilentlyRejected() throws Exception {
            Card land = new Card();
            land.setName("Test Plains");
            land.setType(CardType.LAND);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(land);

            createEngine().handleMessage("GAME_STATE", "");

            verify(mockMessageHandler).handlePlayCard(any(), any());
            verify(mockMessageHandler).handlePassPriority(any(), any());
        }

        @Test
        @DisplayName("AI does NOT pass priority when land play succeeds")
        void doesNotPassPriorityWhenLandPlaySucceeds() throws Exception {
            Card land = new Card();
            land.setName("Test Plains");
            land.setType(CardType.LAND);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(land);

            Mockito.doAnswer(inv -> {
                mockGd.playerHands.get(mockAiPlayer.getId()).removeFirst();
                return null;
            }).when(mockMessageHandler).handlePlayCard(any(), any());

            createEngine().handleMessage("GAME_STATE", "");

            verify(mockMessageHandler).handlePlayCard(any(), any());
            verify(mockMessageHandler, never()).handlePassPriority(any(), any());
        }
    }
}


