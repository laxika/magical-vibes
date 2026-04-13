package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.a.AngelicBlessing;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.AwakenerDruid;
import com.github.laxika.magicalvibes.cards.a.AvenCloudchaser;
import com.github.laxika.magicalvibes.cards.b.BairdStewardOfArgive;
import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.r.RootboundCrag;
import com.github.laxika.magicalvibes.cards.s.SunpetalGrove;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
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
import com.github.laxika.magicalvibes.cards.s.Slagstorm;
import com.github.laxika.magicalvibes.cards.s.SteelSabotage;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.cards.u.UnburialRites;
import com.github.laxika.magicalvibes.cards.v.Vivisection;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.VirtualManaPool;
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
import org.junit.jupiter.api.Tag;
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

@Tag("scryfall")
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
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
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

    /**
     * Adds a single untapped land of the given card class to a player's battlefield.
     */
    private Permanent addUntappedLand(Player player, Class<? extends Card> cardClass) {
        try {
            Permanent perm = new Permanent(cardClass.getDeclaredConstructor().newInstance());
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(perm);
            return perm;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        ai.handleMessage("CHOOSE_MULTIPLE_CARDS", "");
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
        ai.handleMessage("CHOOSE_MULTIPLE_CARDS", "");

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

    @Test
    @DisplayName("AI casts Skaab Ruinator when graveyard has exactly 3 creature cards")
    void castsSkaabRuinatorWithExactlyThreeCreatures() {
        giveAiPriority();
        giveAiIslands(3); // Skaab Ruinator costs {1}{U}{U}

        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());

        harness.setHand(aiPlayer, List.of(new SkaabRuinator()));

        ai.handleMessage("GAME_STATE", "");

        // AI should cast — 3 creature cards exiled from graveyard
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skaab Ruinator");
        assertThat(gd.playerGraveyards.get(aiPlayer.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(aiPlayer.getId())).hasSize(3);
    }

    @Test
    @DisplayName("AI casts Skaab Ruinator when graveyard has more than 3 creature cards")
    void castsSkaabRuinatorWithMoreThanThreeCreatures() {
        giveAiPriority();
        giveAiIslands(3);

        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());

        harness.setHand(aiPlayer, List.of(new SkaabRuinator()));

        ai.handleMessage("GAME_STATE", "");

        // AI should cast — exiles exactly 3 of the 4 creatures
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skaab Ruinator");
        assertThat(gd.getPlayerExiledCards(aiPlayer.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(aiPlayer.getId())).hasSize(1);
    }

    @Test
    @DisplayName("AI casts Skaab Ruinator selecting only creatures from mixed graveyard")
    void castsSkaabRuinatorSelectsOnlyCreaturesFromMixedGraveyard() {
        giveAiPriority();
        giveAiIslands(3);

        // Mix of creatures and non-creatures — 3 creatures + 2 non-creatures
        gd.playerGraveyards.get(aiPlayer.getId()).add(new HolyDay());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new HolyDay());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());

        harness.setHand(aiPlayer, List.of(new SkaabRuinator()));

        ai.handleMessage("GAME_STATE", "");

        // AI should cast — it found 3 creature cards despite non-creatures mixed in
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skaab Ruinator");
        assertThat(gd.getPlayerExiledCards(aiPlayer.getId())).hasSize(3);
        // Only creatures were exiled — the 2 non-creatures remain
        assertThat(gd.playerGraveyards.get(aiPlayer.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(aiPlayer.getId()))
                .allMatch(c -> c.getName().equals("Holy Day"));
    }

    // ===== selectNGraveyardIndicesToExile =====

    @Test
    @DisplayName("selectNGraveyardIndicesToExile returns first N matching indices")
    void selectNGraveyardIndicesToExileReturnsFirstNMatching() {
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());

        var cost = new com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost(3, CardType.CREATURE);
        List<Integer> result = ai.selectNGraveyardIndicesToExile(gd, cost);

        assertThat(result).containsExactly(0, 1, 2);
    }

    @Test
    @DisplayName("selectNGraveyardIndicesToExile skips non-matching types")
    void selectNGraveyardIndicesToExileSkipsNonMatchingTypes() {
        gd.playerGraveyards.get(aiPlayer.getId()).add(new HolyDay());       // index 0: instant
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());  // index 1: creature
        gd.playerGraveyards.get(aiPlayer.getId()).add(new HolyDay());       // index 2: instant
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());  // index 3: creature
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());  // index 4: creature

        var cost = new com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost(3, CardType.CREATURE);
        List<Integer> result = ai.selectNGraveyardIndicesToExile(gd, cost);

        assertThat(result).containsExactly(1, 3, 4);
    }

    @Test
    @DisplayName("selectNGraveyardIndicesToExile returns null when not enough matching cards")
    void selectNGraveyardIndicesToExileReturnsNullWhenNotEnough() {
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new HolyDay());

        var cost = new com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost(3, CardType.CREATURE);
        List<Integer> result = ai.selectNGraveyardIndicesToExile(gd, cost);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("selectNGraveyardIndicesToExile with null requiredType matches any card")
    void selectNGraveyardIndicesToExileWithNullTypeMatchesAny() {
        gd.playerGraveyards.get(aiPlayer.getId()).add(new HolyDay());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());

        var cost = new com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost(2, null);
        List<Integer> result = ai.selectNGraveyardIndicesToExile(gd, cost);

        assertThat(result).containsExactly(0, 1);
    }

    // ===== findExileNGraveyardCost =====

    @Test
    @DisplayName("findExileNGraveyardCost returns cost for Skaab Ruinator")
    void findExileNGraveyardCostReturnsCostForSkaabRuinator() {
        var result = ai.findExileNGraveyardCost(new SkaabRuinator());

        assertThat(result).isNotNull();
        assertThat(result.count()).isEqualTo(3);
        assertThat(result.requiredType()).isEqualTo(CardType.CREATURE);
    }

    @Test
    @DisplayName("findExileNGraveyardCost returns null for card without that cost")
    void findExileNGraveyardCostReturnsNullForNormalCard() {
        var result = ai.findExileNGraveyardCost(new GrizzlyBears());

        assertThat(result).isNull();
    }

    // ===== Modal spell handling (ChooseOneEffect) =====

    @Test
    @DisplayName("findChooseOneEffect returns ChooseOneEffect for modal card")
    void findChooseOneEffectReturnsEffectForModalCard() {
        Card steelSabotage = new SteelSabotage();
        ChooseOneEffect coe = ai.findChooseOneEffect(steelSabotage);
        assertThat(coe).isNotNull();
        assertThat(coe.options()).hasSize(2);
    }

    @Test
    @DisplayName("findChooseOneEffect returns null for non-modal card")
    void findChooseOneEffectReturnsNullForNonModalCard() {
        Card bears = new GrizzlyBears();
        assertThat(ai.findChooseOneEffect(bears)).isNull();
    }

    @Test
    @DisplayName("hasValidModalMode returns true for non-modal card")
    void hasValidModalModeReturnsTrueForNonModalCard() {
        assertThat(ai.hasValidModalMode(gd, new GrizzlyBears())).isTrue();
    }

    @Test
    @DisplayName("hasValidModalMode returns false for Steel Sabotage when no artifacts and no spells on stack")
    void hasValidModalModeReturnsFalseWhenNoValidMode() {
        giveAiPriority();
        // No artifacts on any battlefield, no spells on stack
        assertThat(ai.hasValidModalMode(gd, new SteelSabotage())).isFalse();
    }

    @Test
    @DisplayName("hasValidModalMode returns true for Steel Sabotage when artifact on battlefield")
    void hasValidModalModeReturnsTrueWhenArtifactPresent() {
        giveAiPriority();
        Card artifactCard = new Card();
        artifactCard.setName("Test Artifact");
        artifactCard.setType(CardType.ARTIFACT);
        Permanent artifactPerm = new Permanent(artifactCard);
        gd.playerBattlefields.get(human.getId()).add(artifactPerm);

        assertThat(ai.hasValidModalMode(gd, new SteelSabotage())).isTrue();
    }

    @Test
    @DisplayName("hasValidModalMode returns true for Slagstorm (untargeted modes)")
    void hasValidModalModeReturnsTrueForUntargetedModes() {
        assertThat(ai.hasValidModalMode(gd, new Slagstorm())).isTrue();
    }

    @Test
    @DisplayName("prepareModalSpellCast returns null for non-modal card")
    void prepareModalSpellCastReturnsNullForNonModalCard() {
        assertThat(ai.prepareModalSpellCast(gd, new GrizzlyBears())).isNull();
    }

    @Test
    @DisplayName("prepareModalSpellCast returns null for Steel Sabotage when no valid mode")
    void prepareModalSpellCastReturnsNullWhenNoValidMode() {
        giveAiPriority();
        assertThat(ai.prepareModalSpellCast(gd, new SteelSabotage())).isNull();
    }

    @Test
    @DisplayName("prepareModalSpellCast returns mode 1 (bounce) and artifact target for Steel Sabotage")
    void prepareModalSpellCastReturnsBounceMode() {
        giveAiPriority();
        Card artifactCard = new Card();
        artifactCard.setName("Test Artifact");
        artifactCard.setType(CardType.ARTIFACT);
        Permanent artifactPerm = new Permanent(artifactCard);
        gd.playerBattlefields.get(human.getId()).add(artifactPerm);

        var plan = ai.prepareModalSpellCast(gd, new SteelSabotage());
        assertThat(plan).isNotNull();
        // Mode 0 is "counter artifact spell" (canTargetSpell → skipped), mode 1 is "bounce artifact"
        assertThat(plan.modeIndex()).isEqualTo(1);
        assertThat(plan.targetId()).isEqualTo(artifactPerm.getId());
    }

    @Test
    @DisplayName("prepareModalSpellCast returns mode 0 with null target for Slagstorm")
    void prepareModalSpellCastReturnsUntargetedMode() {
        var plan = ai.prepareModalSpellCast(gd, new Slagstorm());
        assertThat(plan).isNotNull();
        assertThat(plan.modeIndex()).isEqualTo(0);
        assertThat(plan.targetId()).isNull();
    }

    @Test
    @DisplayName("AI does not cast Steel Sabotage when no mode has valid targets")
    void doesNotCastSteelSabotageWhenNoValidMode() {
        giveAiPriority();
        giveAiIslands(1);

        harness.setHand(aiPlayer, List.of(new SteelSabotage()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI casts Steel Sabotage to bounce artifact on opponent's battlefield")
    void castsSteelSabotageToBounceArtifact() {
        giveAiPriority();
        giveAiIslands(1);

        Card artifactCard = new Card();
        artifactCard.setName("Test Artifact");
        artifactCard.setType(CardType.ARTIFACT);
        Permanent artifactPerm = new Permanent(artifactCard);
        gd.playerBattlefields.get(human.getId()).add(artifactPerm);

        harness.setHand(aiPlayer, List.of(new SteelSabotage()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Steel Sabotage");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(artifactPerm.getId());
        // xValue should be 1 (mode index for "bounce artifact")
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(0);
    }

    @Test
    @DisplayName("AI casts Slagstorm (untargeted modal spell)")
    void castsSlagstorm() {
        giveAiPriority();
        giveAiMountains(3);

        harness.setHand(aiPlayer, List.of(new Slagstorm()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Slagstorm");
    }

    @Test
    @DisplayName("AI skips Steel Sabotage (no valid mode) and casts another available spell")
    void skipsModalSpellAndCastsAlternative() {
        giveAiPriority();

        // 1 Island + 1 Forest: enough for either Steel Sabotage ({U}) or Grizzly Bears ({1}{G})
        Permanent island = new Permanent(new Island());
        island.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(island);

        Permanent forest = new Permanent(new Forest());
        forest.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(forest);

        // No artifacts on battlefield → Steel Sabotage has no valid mode
        harness.setHand(aiPlayer, List.of(new SteelSabotage(), new GrizzlyBears()));

        ai.handleMessage("GAME_STATE", "");

        // Should skip Steel Sabotage and cast Grizzly Bears
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Mana tapping before spell casting =====

    /**
     * Adds the given number of untapped Forests to the AI's battlefield.
     */
    private void giveAiForests(int count) {
        for (int i = 0; i < count; i++) {
            Permanent forest = new Permanent(new Forest());
            forest.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(forest);
        }
    }

    @Test
    @DisplayName("AI taps lands before casting spell when mana pool is empty")
    void tapsLandsBeforeCastingSpell() {
        giveAiPriority();
        giveAiForests(2); // GrizzlyBears costs {1}{G}

        // Mana pool is empty — AI must tap Forests to cast
        assertThat(gd.playerManaPools.get(aiPlayer.getId()).getTotal()).isZero();

        harness.setHand(aiPlayer, List.of(new GrizzlyBears()));

        ai.handleMessage("GAME_STATE", "");

        // Grizzly Bears should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");

        // Both Forests should now be tapped
        long tappedCount = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isTapped)
                .count();
        assertThat(tappedCount).isEqualTo(2);
    }

    @Test
    @DisplayName("AI taps only enough lands to pay for spell cost")
    void tapsOnlyEnoughLands() {
        giveAiPriority();
        giveAiForests(5); // 5 Forests available, spell costs {1}{G} = 2

        harness.setHand(aiPlayer, List.of(new GrizzlyBears()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");

        // Only 2 of the 5 lands should be tapped (spell costs 2 mana)
        long tappedCount = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isTapped)
                .count();
        assertThat(tappedCount).isEqualTo(2);
    }

    @Test
    @DisplayName("AI does not re-tap lands that are already tapped")
    void doesNotReTapAlreadyTappedLands() {
        giveAiPriority();

        // 1 tapped Forest + 2 untapped Forests
        Permanent tappedForest = new Permanent(new Forest());
        tappedForest.setSummoningSick(false);
        tappedForest.tap();
        gd.playerBattlefields.get(aiPlayer.getId()).add(tappedForest);

        giveAiForests(2);

        harness.setHand(aiPlayer, List.of(new GrizzlyBears()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);

        // The previously-tapped Forest should remain tapped, plus 2 newly tapped = 3 total tapped
        long tappedCount = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isTapped)
                .count();
        assertThat(tappedCount).isEqualTo(3);
    }

    @Test
    @DisplayName("AI taps lands for X spell with correct total mana")
    void tapsLandsForXSpell() {
        giveAiPriority();
        giveAiMountains(3); // 3 Mountains for Blaze {X}{R}, X will be chosen by AI

        harness.setHand(aiPlayer, List.of(new Blaze()));

        ai.handleMessage("GAME_STATE", "");

        // Blaze should be on the stack (targeting opponent)
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Blaze");

        // All Mountains should be tapped (AI uses all available mana for X)
        long tappedCount = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isTapped)
                .count();
        assertThat(tappedCount).isEqualTo(3);
    }

    @Test
    @DisplayName("AI taps creature mana dorks for requiresCreatureMana spell")
    void tapsCreatureManaDorksForCreatureManaSpell() {
        giveAiPriority();

        // Add two Llanowar Elves (creature mana dorks)
        Permanent elf1 = new Permanent(new LlanowarElves());
        elf1.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(elf1);

        Permanent elf2 = new Permanent(new LlanowarElves());
        elf2.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(elf2);

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.m.MyrSuperion()));

        ai.handleMessage("GAME_STATE", "");

        // Myr Superion should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Myr Superion");

        // Both elves should be tapped
        assertThat(elf1.isTapped()).isTrue();
        assertThat(elf2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("AI skips tapping if mana pool already has enough mana")
    void skipsTappingWhenManaPoolSufficient() {
        giveAiPriority();
        giveAiForests(2); // Untapped but shouldn't be needed

        // Pre-fill mana pool with enough mana
        harness.addMana(aiPlayer, ManaColor.GREEN, 1);
        harness.addMana(aiPlayer, ManaColor.COLORLESS, 1);

        harness.setHand(aiPlayer, List.of(new GrizzlyBears()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);

        // Forests should remain untapped — mana pool already had enough
        long tappedCount = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isTapped)
                .count();
        assertThat(tappedCount).isZero();
    }

    @Test
    @DisplayName("AI taps lands before casting instant")
    void tapsLandsBeforeCastingInstant() {
        giveAiPriority();
        giveAiPlains(1);

        // Mana pool is empty — AI must tap Plains
        assertThat(gd.playerManaPools.get(aiPlayer.getId()).getTotal()).isZero();

        // Only an instant in hand — Easy AI falls through to tryCastInstant
        harness.setHand(aiPlayer, List.of(new HolyDay()));

        ai.handleMessage("GAME_STATE", "");

        // Holy Day should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Holy Day");

        // Plains should be tapped
        long tappedCount = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isTapped)
                .count();
        assertThat(tappedCount).isEqualTo(1);
    }

    // ===== Attack tax handling =====

    @Test
    @DisplayName("getMaxAffordableAttackers returns MAX_VALUE when no tax")
    void maxAffordableAttackersNoTax() {
        assertThat(ai.getMaxAffordableAttackers(gd)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    @DisplayName("getMaxAffordableAttackers limits based on available mana and tax")
    void maxAffordableAttackersWithTax() {
        // Baird imposes {1} per attacker tax
        Permanent baird = new Permanent(new BairdStewardOfArgive());
        baird.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(baird);

        // AI has 3 untapped Plains = 3 virtual mana
        giveAiPlains(3);

        assertThat(ai.getMaxAffordableAttackers(gd)).isEqualTo(3);
    }

    @Test
    @DisplayName("getMaxAffordableAttackers returns 0 when no mana available")
    void maxAffordableAttackersNoMana() {
        Permanent baird = new Permanent(new BairdStewardOfArgive());
        baird.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(baird);

        // No lands/mana for AI
        assertThat(ai.getMaxAffordableAttackers(gd)).isEqualTo(0);
    }

    @Test
    @DisplayName("getMaxAffordableAttackers accounts for dual land flexible overcount")
    void maxAffordableAttackersDualLands() {
        // Baird imposes {1} per attacker tax
        Permanent baird = new Permanent(new BairdStewardOfArgive());
        baird.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(baird);

        // 2 Rootbound Crags = 2 actual mana (each produces R or G, but only 1 per land)
        // Virtual pool total would be 4 (R+G per land) but effective is 2
        addUntappedLand(aiPlayer, RootboundCrag.class);
        addUntappedLand(aiPlayer, RootboundCrag.class);

        // Should be 2, not 4 (which would be wrong without the flexibleOvercount correction)
        assertThat(ai.getMaxAffordableAttackers(gd)).isEqualTo(2);
    }

    @Test
    @DisplayName("prepareAttackersForTax caps attackers to affordable count")
    void prepareAttackersForTaxCapsAttackers() {
        Permanent baird = new Permanent(new BairdStewardOfArgive());
        baird.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(baird);

        // AI has 2 untapped Plains = can afford 2 attackers at {1} each
        giveAiPlains(2);

        List<Integer> requested = List.of(0, 1, 2, 3);
        List<Integer> result = ai.prepareAttackersForTax(gd, requested);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(0, 1);
    }

    @Test
    @DisplayName("prepareAttackersForTax returns all attackers when affordable")
    void prepareAttackersForTaxReturnsAllWhenAffordable() {
        Permanent baird = new Permanent(new BairdStewardOfArgive());
        baird.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(baird);

        // 3 Plains = can afford 3 attackers at {1} each
        giveAiPlains(3);

        List<Integer> result = ai.prepareAttackersForTax(gd, List.of(0, 1));

        // Both attackers should be kept (affordable)
        assertThat(result).containsExactly(0, 1);
    }

    @Test
    @DisplayName("prepareAttackersForTax returns empty list when no mana for tax")
    void prepareAttackersForTaxReturnsEmptyWhenBroke() {
        Permanent baird = new Permanent(new BairdStewardOfArgive());
        baird.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(baird);

        // No lands/mana for AI
        List<Integer> result = ai.prepareAttackersForTax(gd, List.of(0, 1));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("prepareAttackersForTax passes through when no tax")
    void prepareAttackersForTaxNoOp() {
        List<Integer> original = List.of(0, 1, 2);
        List<Integer> result = ai.prepareAttackersForTax(gd, original);

        assertThat(result).isEqualTo(original);
    }

    @Test
    @DisplayName("AI handleAttackers limits attackers when attack tax is present")
    void handleAttackersLimitsForTax() {
        // Setup: human controls Baird (tax {1} per attacker)
        Permanent baird = new Permanent(new BairdStewardOfArgive());
        baird.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(baird);

        // AI has 3 creatures and only 1 Plains
        giveAiPlains(1);
        for (int i = 0; i < 3; i++) {
            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(bears);
        }

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(aiPlayer.getId());

        ai.handleMessage("AVAILABLE_ATTACKERS", "");

        // At most 1 creature should be attacking (can only afford {1} tax)
        long attackingCount = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isAttacking)
                .count();
        assertThat(attackingCount).isLessThanOrEqualTo(1);
    }

    // ===== prepareAttackersForTax skips choice-triggering mana sources =====

    @Test
    @DisplayName("prepareAttackersForTax skips Birds of Paradise to avoid color choice")
    void prepareAttackersForTaxSkipsBirdsOfParadise() {
        // Setup: opponent controls Baird (tax {1} per attacker)
        Permanent baird = new Permanent(new BairdStewardOfArgive());
        baird.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(baird);

        // AI has 1 Plains + 1 Birds of Paradise
        // With safe pool: only 1 mana from Plains → can afford 1 attacker
        // Without safe pool: 2 mana (Plains + BoP) → would try 2 attackers
        giveAiPlains(1);
        Permanent bop = new Permanent(new BirdsOfParadise());
        bop.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bop);

        List<Integer> result = ai.prepareAttackersForTax(gd, List.of(0, 1, 2));

        // Should cap at 1 attacker (only Plains counted in safe pool)
        assertThat(result).hasSize(1);
        // Birds of Paradise should NOT be tapped
        assertThat(bop.isTapped()).isFalse();
        // Interaction state should still be null (no color choice triggered)
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("prepareAttackersForTax removes attackers that were tapped as mana sources")
    void prepareAttackersForTaxRemovesTappedManaCreatures() {
        // Setup: opponent controls Baird (tax {1} per attacker)
        Permanent baird = new Permanent(new BairdStewardOfArgive());
        baird.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(baird);

        // AI battlefield: LlanowarElves (creature mana source) + GrizzlyBears (vanilla creature)
        // No lands — only LlanowarElves can produce mana for the tax.
        Permanent elves = new Permanent(new LlanowarElves());
        elves.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(elves);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears);

        // Set up ATTACKER_DECLARATION state so tapping for tax is allowed
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gd.interaction.beginAttackerDeclaration(aiPlayer.getId());

        // Both creatures are at indices 0 (elves) and 1 (bears) — request both as attackers
        // Tax for 2 attackers = {2}, but only 1 mana available from elves.
        // prepareAttackersForTax caps to 1, taps elves for mana, then should remove elves
        // from the attacker list since it's now tapped.
        List<Integer> result = ai.prepareAttackersForTax(gd, List.of(0, 1));

        assertThat(elves.isTapped()).isTrue();
        // Elves (index 0) should be removed because it was tapped for mana
        assertThat(result).doesNotContain(0);
    }

    @Test
    @DisplayName("prepareAttackersForTax keeps attackers that were not tapped for mana")
    void prepareAttackersForTaxKeepsUntappedAttackers() {
        // Setup: opponent controls Baird (tax {1} per attacker)
        Permanent baird = new Permanent(new BairdStewardOfArgive());
        baird.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(baird);

        // AI has: 1 Plains (mana source) + 1 GrizzlyBears (attacker)
        giveAiPlains(1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears);

        // Set up ATTACKER_DECLARATION state so tapping for tax is allowed
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gd.interaction.beginAttackerDeclaration(aiPlayer.getId());

        // Plains at index 0, Bears at index 1 — only Bears is an attacker
        // Tax for 1 attacker = {1}, paid by tapping Plains
        List<Integer> result = ai.prepareAttackersForTax(gd, List.of(1));

        assertThat(bears.isTapped()).isFalse();
        assertThat(result).containsExactly(1);
    }

    @Test
    @DisplayName("prepareAttackersForTax removes all attackers when all were tapped for mana")
    void prepareAttackersForTaxRemovesAllWhenAllTapped() {
        // Setup: opponent controls Baird (tax {1} per attacker)
        Permanent baird = new Permanent(new BairdStewardOfArgive());
        baird.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(baird);

        // AI has only 1 LlanowarElves — it's both the attacker and the only mana source.
        Permanent elves = new Permanent(new LlanowarElves());
        elves.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(elves);

        // Set up ATTACKER_DECLARATION state so tapping for tax is allowed
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gd.interaction.beginAttackerDeclaration(aiPlayer.getId());

        // Request elves (index 0) as sole attacker. Tax = {1}, paid by tapping elves itself.
        List<Integer> result = ai.prepareAttackersForTax(gd, List.of(0));

        assertThat(elves.isTapped()).isTrue();
        assertThat(result).isEmpty();
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
                    mockTargetValidationService, null);
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

    // ===== hasPermanentManaValueEqualsXTarget =====

    @Test
    @DisplayName("hasPermanentManaValueEqualsXTarget returns true for Entrancing Melody")
    void hasPermanentManaValueEqualsXTargetTrueForEntrancingMelody() {
        assertThat(ai.hasPermanentManaValueEqualsXTarget(new EntrancingMelody())).isTrue();
    }

    @Test
    @DisplayName("hasPermanentManaValueEqualsXTarget returns false for non-X-targeting spell")
    void hasPermanentManaValueEqualsXTargetFalseForGrizzlyBears() {
        assertThat(ai.hasPermanentManaValueEqualsXTarget(new GrizzlyBears())).isFalse();
    }

    @Test
    @DisplayName("hasPermanentManaValueEqualsXTarget returns false for X damage spell (Blaze)")
    void hasPermanentManaValueEqualsXTargetFalseForBlaze() {
        assertThat(ai.hasPermanentManaValueEqualsXTarget(new Blaze())).isFalse();
    }

    // ===== AI casts Entrancing Melody with correct X and target =====

    @Test
    @DisplayName("AI casts Entrancing Melody setting X to match target creature's mana value")
    void castsEntrancingMelodyWithCorrectXForTarget() {
        giveAiPriority();
        // Entrancing Melody costs {X}{U}{U}. With 4 Islands, maxX = 2.
        giveAiIslands(4);

        // Opponent has Grizzly Bears (MV=2) — affordable at X=2
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new EntrancingMelody()));

        ai.handleMessage("GAME_STATE", "");

        // AI should cast with target = bears and X = 2
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Entrancing Melody");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("AI skips Entrancing Melody when no creature has affordable mana value")
    void skipsEntrancingMelodyWhenTargetTooExpensive() {
        giveAiPriority();
        // With 3 Islands, maxX = 1 (3 - 2 for {U}{U} = 1)
        giveAiIslands(3);

        // Opponent has only Grizzly Bears (MV=2) — too expensive at maxX=1
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new EntrancingMelody()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — can't afford X=2 for the bears
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI picks highest mana value target it can afford for Entrancing Melody")
    void picksHighestAffordableManaValueTarget() {
        giveAiPriority();
        // With 4 Islands, maxX = 2
        giveAiIslands(4);

        // Opponent has EliteVanguard (MV=1) and GrizzlyBears (MV=2)
        Permanent vanguard = new Permanent(new EliteVanguard());
        vanguard.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(vanguard);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new EntrancingMelody()));

        ai.handleMessage("GAME_STATE", "");

        // AI should pick the highest affordable target (MV=2 bears over MV=1 vanguard)
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("AI targets lower MV creature when higher one is unaffordable")
    void targetsLowerMvWhenHigherUnaffordable() {
        giveAiPriority();
        // With 3 Islands, maxX = 1
        giveAiIslands(3);

        // Opponent has EliteVanguard (MV=1) and GrizzlyBears (MV=2)
        Permanent vanguard = new Permanent(new EliteVanguard());
        vanguard.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(vanguard);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new EntrancingMelody()));

        ai.handleMessage("GAME_STATE", "");

        // Can only afford X=1, so should target EliteVanguard
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(vanguard.getId());
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(1);
    }

    @Test
    @DisplayName("AI skips Entrancing Melody when opponent has no creatures")
    void skipsEntrancingMelodyWhenNoCreatures() {
        giveAiPriority();
        giveAiIslands(6);

        harness.setHand(aiPlayer, List.of(new EntrancingMelody()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).isEmpty();
    }

    // ===== Dual land mana — virtual pool estimation =====

    @Test
    @DisplayName("Virtual pool includes mana from Rootbound Crag")
    void virtualPoolIncludesDualLandMana() {
        addUntappedLand(aiPlayer, RootboundCrag.class);

        AiManaManager manaManager = new AiManaManager(harness.getGameQueryService());
        ManaPool pool = manaManager.buildVirtualManaPool(gd, aiPlayer.getId());

        assertThat(pool.get(ManaColor.RED)).isGreaterThanOrEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Virtual pool total is correct for two Rootbound Crags (no over-count)")
    void virtualPoolTotalCorrectForTwoDualLands() {
        addUntappedLand(aiPlayer, RootboundCrag.class);
        addUntappedLand(aiPlayer, RootboundCrag.class);

        AiManaManager manaManager = new AiManaManager(harness.getGameQueryService());
        VirtualManaPool pool = manaManager.buildVirtualManaPool(gd, aiPlayer.getId());

        assertThat(pool.get(ManaColor.RED)).isEqualTo(2);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(2);
        // Effective total = 2 (correct for 2 lands, each producing R or G but only 1 per land)
        assertThat(pool.getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Virtual pool correctly mixes basic and dual lands")
    void virtualPoolMixesBasicAndDualLands() {
        addUntappedLand(aiPlayer, Mountain.class);
        addUntappedLand(aiPlayer, RootboundCrag.class);

        AiManaManager manaManager = new AiManaManager(harness.getGameQueryService());
        VirtualManaPool pool = manaManager.buildVirtualManaPool(gd, aiPlayer.getId());

        assertThat(pool.get(ManaColor.RED)).isEqualTo(2); // Mountain + Crag
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1); // Crag only
        assertThat(pool.getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Virtual pool handles pain lands (three activated abilities)")
    void virtualPoolHandlesPainLands() {
        addUntappedLand(aiPlayer, YavimayaCoast.class);

        AiManaManager manaManager = new AiManaManager(harness.getGameQueryService());
        VirtualManaPool pool = manaManager.buildVirtualManaPool(gd, aiPlayer.getId());

        // Yavimaya Coast: {C}, {G}+damage, {U}+damage
        assertThat(pool.get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isGreaterThanOrEqualTo(1);
        assertThat(pool.get(ManaColor.BLUE)).isGreaterThanOrEqualTo(1);
        // Only 1 land, effective total must be 1
        assertThat(pool.getTotal()).isEqualTo(1);
    }

    // ===== Dual land mana — casting spells =====

    @Test
    @DisplayName("AI casts Goblin Piker ({1}{R}) with two Rootbound Crags")
    void castsGoblinPikerWithTwoRootboundCrags() {
        giveAiPriority();
        addUntappedLand(aiPlayer, RootboundCrag.class);
        addUntappedLand(aiPlayer, RootboundCrag.class);

        harness.setHand(aiPlayer, List.of(new GoblinPiker()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Piker");
    }

    @Test
    @DisplayName("AI casts Grizzly Bears ({1}{G}) with two Rootbound Crags")
    void castsGrizzlyBearsWithTwoRootboundCrags() {
        giveAiPriority();
        addUntappedLand(aiPlayer, RootboundCrag.class);
        addUntappedLand(aiPlayer, RootboundCrag.class);

        harness.setHand(aiPlayer, List.of(new GrizzlyBears()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("AI casts Goblin Piker ({1}{R}) with one Mountain and one Rootbound Crag")
    void castsWithMixedBasicAndDualLands() {
        giveAiPriority();
        addUntappedLand(aiPlayer, Mountain.class);
        addUntappedLand(aiPlayer, RootboundCrag.class);

        harness.setHand(aiPlayer, List.of(new GoblinPiker()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Piker");
    }

    @Test
    @DisplayName("AI casts Grizzly Bears ({1}{G}) with Mountain and Rootbound Crag using intelligent color choice")
    void castsGreenSpellWithMountainAndCragChoosingGreenFromCrag() {
        giveAiPriority();
        addUntappedLand(aiPlayer, Mountain.class);
        addUntappedLand(aiPlayer, RootboundCrag.class);

        harness.setHand(aiPlayer, List.of(new GrizzlyBears()));

        ai.handleMessage("GAME_STATE", "");

        // AI should cast: Rootbound Crag produces {G}, Mountain produces {R} for generic
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("AI does not cast a 3-mana spell with only two dual lands")
    void doesNotCastThreeCostWithTwoDualLands() {
        giveAiPriority();
        addUntappedLand(aiPlayer, RootboundCrag.class);
        addUntappedLand(aiPlayer, RootboundCrag.class);

        // Awakener Druid costs {2}{G} = 3 mana, but we only have 2 lands
        harness.setHand(aiPlayer, List.of(new AwakenerDruid()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI casts with single Rootbound Crag for {R} spell — not enough for {1}{R}")
    void castsOneRedManaSpellWithSingleCrag() {
        giveAiPriority();
        addUntappedLand(aiPlayer, RootboundCrag.class);

        harness.setHand(aiPlayer, List.of(new GoblinPiker())); // costs {1}{R} = 2 mana

        ai.handleMessage("GAME_STATE", "");

        // Only 1 land = 1 mana, can't cast {1}{R}
        assertThat(gd.stack).isEmpty();
    }

    // ===== Dual land mana — intelligent color selection =====

    @Test
    @DisplayName("AI taps dual land for needed color and basic land for generic")
    void tapsCorrectColorsWithMixedLands() {
        giveAiPriority();
        addUntappedLand(aiPlayer, Forest.class);
        addUntappedLand(aiPlayer, RootboundCrag.class);

        harness.setHand(aiPlayer, List.of(new GoblinPiker()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Piker");
        // Both lands should be tapped
        long tappedCount = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isTapped).count();
        assertThat(tappedCount).isEqualTo(2);
    }

    @Test
    @DisplayName("AI prefers colorless over pain ability when color not needed")
    void prefersPainlessAbilityForGenericCosts() {
        giveAiPriority();
        addUntappedLand(aiPlayer, Forest.class);
        addUntappedLand(aiPlayer, YavimayaCoast.class);

        harness.setHand(aiPlayer, List.of(new GrizzlyBears()));

        int lifeBefore = gd.playerLifeTotals.get(aiPlayer.getId());

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
        // AI should not have taken pain land damage (used {C} ability for generic)
        int lifeAfter = gd.playerLifeTotals.get(aiPlayer.getId());
        assertThat(lifeAfter).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("AI uses pain ability when needed for colored cost")
    void usesPainAbilityWhenColorIsNeeded() {
        giveAiPriority();
        addUntappedLand(aiPlayer, YavimayaCoast.class);
        addUntappedLand(aiPlayer, YavimayaCoast.class);

        harness.setHand(aiPlayer, List.of(new GrizzlyBears()));

        int lifeBefore = gd.playerLifeTotals.get(aiPlayer.getId());

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
        // AI should take exactly 1 damage (one pain tap for {G}, one {C} tap)
        int lifeAfter = gd.playerLifeTotals.get(aiPlayer.getId());
        assertThat(lifeAfter).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("AI casts with multiple different dual lands")
    void castsWithMultipleDifferentDualLands() {
        giveAiPriority();
        addUntappedLand(aiPlayer, RootboundCrag.class);
        addUntappedLand(aiPlayer, SunpetalGrove.class);

        harness.setHand(aiPlayer, List.of(new GrizzlyBears()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("AI taps both lands correctly for two-color needs")
    void tapsBothLandsForTwoColorNeeds() {
        giveAiPriority();
        addUntappedLand(aiPlayer, RootboundCrag.class);
        addUntappedLand(aiPlayer, RootboundCrag.class);
        addUntappedLand(aiPlayer, Forest.class);

        harness.setHand(aiPlayer, List.of(new GoblinPiker()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Piker");
    }

    // ===== Dual land mana — edge cases =====

    @Test
    @DisplayName("Tapped dual land does not contribute to virtual pool")
    void tappedDualLandNotInVirtualPool() {
        Permanent crag = addUntappedLand(aiPlayer, RootboundCrag.class);
        crag.tap();

        AiManaManager manaManager = new AiManaManager(harness.getGameQueryService());
        ManaPool pool = manaManager.buildVirtualManaPool(gd, aiPlayer.getId());

        assertThat(pool.get(ManaColor.RED)).isEqualTo(0);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("AI can still use basic lands alongside dual lands")
    void basicLandsStillWorkAlongsideDualLands() {
        giveAiPriority();
        addUntappedLand(aiPlayer, Mountain.class);
        addUntappedLand(aiPlayer, Mountain.class);
        addUntappedLand(aiPlayer, RootboundCrag.class);

        // Berserkers of Blood Ridge costs {3}{R}{R} — needs 5 mana
        // With 3 lands we can't afford it
        harness.setHand(aiPlayer, List.of(new BerserkersOfBloodRidge()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI correctly rejects unaffordable spell with many dual lands")
    void rejectsUnaffordableSpellWithDualLands() {
        giveAiPriority();
        for (int i = 0; i < 4; i++) {
            addUntappedLand(aiPlayer, RootboundCrag.class);
        }

        harness.setHand(aiPlayer, List.of(new BerserkersOfBloodRidge()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI casts affordable spell with enough dual lands")
    void castsAffordableSpellWithEnoughDualLands() {
        giveAiPriority();
        for (int i = 0; i < 5; i++) {
            addUntappedLand(aiPlayer, RootboundCrag.class);
        }

        harness.setHand(aiPlayer, List.of(new BerserkersOfBloodRidge()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Berserkers of Blood Ridge");
    }

    // ===== tapManaForSpell awaiting input =====

    @Test
    @DisplayName("tapManaForSpell returns false when mana pool already has enough")
    void tapManaForSpellReturnsFalseWhenPoolSufficient() {
        giveAiPriority();
        harness.addMana(aiPlayer, ManaColor.GREEN, 1);
        harness.addMana(aiPlayer, ManaColor.COLORLESS, 1);

        Card bears = new GrizzlyBears();
        boolean result = ai.tapManaForSpell(gd, bears, null);

        assertThat(result).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("tapManaForSpell returns false after normal land tapping")
    void tapManaForSpellReturnsFalseAfterNormalTapping() {
        giveAiPriority();
        addUntappedLand(aiPlayer, Forest.class);
        addUntappedLand(aiPlayer, Forest.class);

        Card bears = new GrizzlyBears();
        boolean result = ai.tapManaForSpell(gd, bears, null);

        assertThat(result).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("tapManaForSpell returns false for card with no mana cost")
    void tapManaForSpellReturnsFalseForNoManaCost() {
        giveAiPriority();

        Card land = new Plains();
        boolean result = ai.tapManaForSpell(gd, land, null);

        assertThat(result).isFalse();
    }
}
