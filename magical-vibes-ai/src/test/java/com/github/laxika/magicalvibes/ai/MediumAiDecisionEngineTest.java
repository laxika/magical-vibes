package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.testutil.TestCards;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AetherTide;
import com.github.laxika.magicalvibes.cards.a.AjanisResponse;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.ArchangelOfTithes;
import com.github.laxika.magicalvibes.cards.a.AlphaAuthority;
import com.github.laxika.magicalvibes.cards.t.TroveOfTemptation;
import com.github.laxika.magicalvibes.cards.t.Tromokratis;
import com.github.laxika.magicalvibes.cards.t.TolarianScholar;
import com.github.laxika.magicalvibes.cards.t.TorgaarFamineIncarnate;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BairdStewardOfArgive;
import com.github.laxika.magicalvibes.cards.b.BlindingBeam;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.c.ChampionOfThePath;
import com.github.laxika.magicalvibes.cards.c.CatharticReunion;
import com.github.laxika.magicalvibes.cards.c.CrypticCommand;
import com.github.laxika.magicalvibes.cards.c.CurseOfEchoes;
import com.github.laxika.magicalvibes.cards.c.Crawlspace;
import com.github.laxika.magicalvibes.cards.d.Dominate;
import com.github.laxika.magicalvibes.cards.d.DauthiMercenary;
import com.github.laxika.magicalvibes.cards.d.Drought;
import com.github.laxika.magicalvibes.cards.d.DreamHalls;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.EkunduCyclops;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.e.Errantry;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.Hipparion;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.h.HootingMandrills;
import com.github.laxika.magicalvibes.cards.m.Mindslaver;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IslandSanctuary;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.k.KjeldoranRoyalGuard;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.Lure;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.s.Slagstorm;
import com.github.laxika.magicalvibes.cards.s.SteelSabotage;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TorrentOfSouls;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.Pyrokinesis;
import com.github.laxika.magicalvibes.cards.p.PyrrhicStrike;
import com.github.laxika.magicalvibes.cards.r.ReignOfChaos;
import com.github.laxika.magicalvibes.cards.r.Ramroller;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SelectiveSnare;
import com.github.laxika.magicalvibes.cards.o.OrcishConscripts;
import com.github.laxika.magicalvibes.cards.o.Okk;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.u.Unbury;
import com.github.laxika.magicalvibes.cards.v.Victimize;
import com.github.laxika.magicalvibes.cards.w.WearTear;

import com.github.laxika.magicalvibes.cards.v.Vivisection;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityService;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("scryfall")
class MediumAiDecisionEngineTest {

    private GameTestHarness harness;
    private Player human;
    private Player aiPlayer;
    private GameData gd;
    private MediumAiDecisionEngine ai;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        human = harness.getPlayer1();
        aiPlayer = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();

        FakeConnection aiConn = new FakeConnection("ai-medium-test");
        harness.getSessionManager().registerPlayer(aiConn, aiPlayer.getId(), "Bob");
        ai = new MediumAiDecisionEngine(gd.id, aiPlayer, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
    }

    private void giveAiPriority() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
    }

    private void giveAiPlains(int count) {
        for (int i = 0; i < count; i++) {
            Permanent plains = new Permanent(new Plains());
            plains.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(plains);
        }
    }

    private void giveAiIslands(int count) {
        for (int i = 0; i < count; i++) {
            Permanent island = new Permanent(new Island());
            island.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(island);
        }
    }

    private void giveAiMountains(int count) {
        for (int i = 0; i < count; i++) {
            Permanent mountain = new Permanent(new Mountain());
            mountain.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(mountain);
        }
    }

    private void giveAiSwamps(int count) {
        for (int i = 0; i < count; i++) {
            Permanent swamp = new Permanent(new Swamp());
            swamp.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(swamp);
        }
    }

    @Test
    @DisplayName("Medium AI chooses a creature type for Selective Snare")
    void castsSelectiveSnareWithCreatureTypeChoice() {
        giveAiPriority();
        giveAiIslands(3);
        Permanent target = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());
        SelectiveSnare snare = new SelectiveSnare();
        harness.setHand(aiPlayer, List.of(snare));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(snare);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
        assertThat(gd.stack.getFirst().getChosenCreatureType()).isEqualTo(CardSubtype.BEAR);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(human.getId())).doesNotContain(target);
    }

    private Card multiTargetRemovalWithPerTargetLifeCost() {
        Card card = new Card();
        card.setName("Multi-target removal with a life cost");
        card.setType(CardType.SORCERY);
        card.setManaCost("{2}{B}{R}");
        card.setAdditionalLifeCostPerTarget(3);
        for (int targetGroup = 0; targetGroup < 3; targetGroup++) {
            PermanentPredicateTargetFilter creatureTarget = new PermanentPredicateTargetFilter(
                    new PermanentIsCreaturePredicate(), "Target must be a creature");
            card.target(creatureTarget, 0, 1)
                    .addEffect(EffectSlot.SPELL, DestroyTargetPermanentEffect.forTargetGroup(targetGroup));
        }
        return card;
    }

    private Card repeatableArtifactRemoval() {
        Card card = new Card();
        card.setName("Repeatable artifact removal");
        card.setType(CardType.SORCERY);
        card.setManaCost("{X}{R}");
        card.addEffect(EffectSlot.SPELL,
                new RepeatableAdditionalManaCost(List.of("{1}{R}", "{1}{G}")));
        card.targetX(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(), "Targets must be artifacts"), 100)
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        return card;
    }

    @Test
    @DisplayName("Medium AI limits multi-target life costs to its available life")
    void limitsPerTargetLifeCostTargets() {
        giveAiPriority();
        giveAiSwamps(2);
        giveAiMountains(2);
        harness.setLife(aiPlayer, 7);
        harness.addToBattlefield(human, new SerraAngel());
        harness.addToBattlefield(human, new GrizzlyBears());
        harness.addToBattlefield(human, new LlanowarElves());
        Card spell = multiTargetRemovalWithPerTargetLifeCost();
        harness.setHand(aiPlayer, List.of(spell));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(gd.stack.getFirst().getTargetIds()).hasSize(2);
        assertThat(gd.getLife(aiPlayer.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Medium AI pays a battlefield-imposed sacrifice tax when casting a black spell")
    void castsBlackSpellWithDroughtTax() {
        harness.addToBattlefield(human, new Drought());
        Permanent swamp = harness.addToBattlefieldAndReturn(aiPlayer, new Swamp());
        DauthiMercenary mercenary = new DauthiMercenary();
        harness.setHand(aiPlayer, List.of(mercenary));
        harness.addMana(aiPlayer, ManaColor.BLACK, 1);
        harness.addMana(aiPlayer, ManaColor.COLORLESS, 2);
        giveAiPriority();

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(mercenary);
        assertThat(gd.playerBattlefields.get(aiPlayer.getId())).doesNotContain(swamp);
    }

    @Test
    @DisplayName("Medium AI supplies Torgaar's sacrifice-based cost reduction")
    void castsTorgaarWithSacrificeCostReduction() {
        Permanent fodder = harness.addToBattlefieldAndReturn(aiPlayer, new TolarianScholar());
        TorgaarFamineIncarnate torgaar = new TorgaarFamineIncarnate();
        harness.setHand(aiPlayer, List.of(torgaar));
        harness.addMana(aiPlayer, ManaColor.BLACK, 2);
        harness.addMana(aiPlayer, ManaColor.COLORLESS, 4);
        giveAiPriority();

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(torgaar);
        assertThat(gd.playerBattlefields.get(aiPlayer.getId())).doesNotContain(fodder);
    }

    @Test
    @DisplayName("Medium AI announces one target when paying no repeatable additional cost")
    void castsRepeatableTargetSpellWithItsBaseTargetCount() {
        giveAiPriority();
        harness.addMana(aiPlayer, ManaColor.RED, 3);
        Permanent artifact = harness.addToBattlefieldAndReturn(human, new Ornithopter());
        Card spell = repeatableArtifactRemoval();
        harness.setHand(aiPlayer, List.of(spell));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(artifact.getId());
        assertThat(gd.stack.getFirst().getRepeatedAdditionalCosts()).isEmpty();
    }

    @Test
    @DisplayName("Medium AI uses Delve cards to reduce a spell's generic mana cost")
    void castsDelveSpellWithGraveyardReduction() {
        giveAiPriority();
        for (int i = 0; i < 4; i++) {
            Permanent forest = new Permanent(new Forest());
            forest.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(forest);
        }

        HootingMandrills mandrills = new HootingMandrills();
        harness.setHand(aiPlayer, List.of(mandrills));
        harness.setGraveyard(aiPlayer, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(mandrills);
        assertThat(gd.playerGraveyards.get(aiPlayer.getId())).hasSize(3);
        assertThat(gd.getPlayerExiledCards(aiPlayer.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Medium AI does not submit a Dream Halls-only alternate cast as a mana cast")
    void skipsDreamHallsOnlyAlternateCast() {
        giveAiPriority();
        harness.addToBattlefield(aiPlayer, new DreamHalls());
        giveAiIslands(3);
        CurseOfEchoes curseOfEchoes = new CurseOfEchoes();
        AirElemental airElemental = new AirElemental();
        harness.setHand(aiPlayer, List.of(curseOfEchoes, airElemental));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(aiPlayer.getId()))
                .containsExactly(curseOfEchoes, airElemental);
        assertThat(gd.playerBattlefields.get(aiPlayer.getId()))
                .allMatch(permanent -> !permanent.isTapped());
    }

    private Card whitePlainsCreature() {
        Card card = new Card();
        card.setName("White Plains Creature");
        card.setType(CardType.LAND);
        card.setAdditionalTypes(Set.of(CardType.CREATURE));
        card.setColors(List.of(CardColor.WHITE));
        card.setSubtypes(List.of(CardSubtype.PLAINS));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    @Test
    @DisplayName("Medium AI uses Pyrokinesis's hand-exile alternate cost")
    void usesHandExileAlternateCost() {
        giveAiPriority();
        Permanent target = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());
        target.setSummoningSick(false);
        Pyrokinesis pyrokinesis = new Pyrokinesis();
        LightningBolt redCard = new LightningBolt();
        harness.setHand(aiPlayer, List.of(pyrokinesis, redCard));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(pyrokinesis);
        assertThat(gd.getPlayerExiledCards(aiPlayer.getId())).containsExactly(redCard);
        assertThat(gd.playerManaPools.get(aiPlayer.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Medium AI casts Pacifism on opponent's biggest threat")
    void castsRemovalOnBiggestThreat() {
        giveAiPriority();
        giveAiPlains(2);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pacifism");
        // Should target the Air Elemental (biggest threat)
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(airElemental.getId());
    }

    @Test
    @DisplayName("Medium AI supplies a matching permanent for a behold additional cost")
    void castsBeholdSpellWithMatchingPermanent() {
        giveAiPriority();
        giveAiMountains(4);
        Permanent elemental = harness.addToBattlefieldAndReturn(aiPlayer, new AirElemental());
        elemental.setSummoningSick(false);
        ChampionOfThePath champion = new ChampionOfThePath();
        harness.setHand(aiPlayer, List.of(champion));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(champion);
        assertThat(gd.getPlayerExiledCards(aiPlayer.getId()))
                .extracting(Card::getId)
                .contains(elemental.getCard().getId());
    }

    @Test
    @DisplayName("Medium AI does not cast Ajani's Response at an unaffordable untapped target")
    void doesNotCastTargetReducedSpellAtUnaffordableTarget() {
        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(human.getId());
        giveAiPlains(1);
        giveAiIslands(2);

        Permanent ownTappedCreature = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        ownTappedCreature.setSummoningSick(false);
        ownTappedCreature.tap();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());
        opponentCreature.setSummoningSick(false);
        harness.setHand(aiPlayer, List.of(new AjanisResponse()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(aiPlayer.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .allMatch(permanent -> !permanent.isTapped());
    }

    @Test
    @DisplayName("Medium AI supplies untapped creatures for convoke")
    void castsConvokeSpellWithUntappedCreatures() {
        giveAiPriority();
        giveAiIslands(2);
        Permanent firstCreature = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        firstCreature.setSummoningSick(false);
        secondCreature.setSummoningSick(false);

        Card convokeSpell = new Card();
        convokeSpell.setName("Convoke Test Creature");
        convokeSpell.setType(CardType.CREATURE);
        convokeSpell.setManaCost("{3}{U}");
        convokeSpell.setPower(4);
        convokeSpell.setToughness(4);
        convokeSpell.setKeywords(EnumSet.of(Keyword.CONVOKE));
        harness.setHand(aiPlayer, List.of(convokeSpell));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(convokeSpell);
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Medium AI does not attack into clearly losing trade")
    void doesNotAttackIntoLosingTrade() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        harness.beginAttackerDeclarationInput();

        // AI has a 2/2
        Permanent aiBears = new Permanent(new GrizzlyBears());
        aiBears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(aiBears);

        // Opponent has a 4/4
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        // AI should not have attacked A?€�t bears would die without killing AE
        // The attack step resolves, check that bears is still alive and untapped
        assertThat(aiBears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Medium AI does not submit a creature barred by Island Sanctuary")
    void doesNotSubmitCreatureBarredByIslandSanctuary() {
        harness.addToBattlefield(human, new IslandSanctuary());
        gd.turnNumber = 2;
        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(human, true);

        Permanent attacker = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        attacker.setSummoningSick(false);
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Medium AI sends a required attacker to an available planeswalker")
    void sendsRequiredAttackerToAvailablePlaneswalker() {
        harness.addToBattlefield(human, new IslandSanctuary());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(human, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        Permanent blocker = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.turnNumber = 2;
        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(human, true);

        Permanent attacker = harness.addToBattlefieldAndReturn(aiPlayer, new Ramroller());
        attacker.setSummoningSick(false);
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.inMutationScope(() -> harness.getCombatAttackService().handleDeclareAttackersStep(gd));

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }
        assertThat(attacker.isAttacking()).isTrue();
        assertThat(attacker.getAttackTarget()).isEqualTo(planeswalker.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Medium AI respects a controller-scoped attacker limit")
    void respectsControllerScopedAttackerLimit() {
        gd.playerLifeTotals.put(human.getId(), 20);
        Permanent limit = harness.addToBattlefieldAndReturn(human, new Crawlspace());
        limit.setSummoningSick(false);
        Permanent first = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        first.setSummoningSick(false);
        Permanent second = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        second.setSummoningSick(false);
        Permanent third = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        third.setSummoningSick(false);

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            ai.sendAttackerDeclaration(new DeclareAttackersRequest(List.of(0, 1, 2), null));

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gd.getLife(human.getId())).isEqualTo(16);
        assertThat(gd.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("declares 2 attackers."));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Medium AI recognizes lethal and attacks all-in")
    void recognizesLethalAllIn() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        harness.beginAttackerDeclarationInput();

        gd.playerLifeTotals.put(human.getId(), 4);

        // AI has two 2/2s (total 4 damage = exact lethal)
        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears1);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears2);

        ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        // Both should be attacking for lethal
        long attackingCount = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isAttacking)
                .count();
        assertThat(attackingCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Medium AI includes an attack-if-another-attacks creature")
    void includesConditionalAttackRequirement() {
        gd.playerLifeTotals.put(human.getId(), 5);
        Permanent cyclops = harness.addToBattlefieldAndReturn(aiPlayer, new EkunduCyclops());
        cyclops.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        bears.setSummoningSick(false);

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(cyclops.isAttacking()).isTrue();
        assertThat(bears.isAttacking()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Medium AI does not declare Orcish Conscripts without enough other attackers")
    void doesNotDeclareOrcishConscriptsWithoutEnoughOtherAttackers() {
        gd.playerLifeTotals.put(human.getId(), 4);
        Permanent conscripts = harness.addToBattlefieldAndReturn(aiPlayer, new OrcishConscripts());
        conscripts.setSummoningSick(false);
        Permanent ally = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        ally.setSummoningSick(false);

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(conscripts.isAttackedThisTurn()).isFalse();
        assertThat(ally.isAttackedThisTurn()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Medium AI removes a creature that can only attack alone from a larger group")
    void removesCanOnlyAttackAloneCreature() {
        Permanent restricted = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        restricted.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(aiPlayer, new Errantry());
        aura.setAttachedTo(restricted.getId());
        Permanent unrestricted = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        unrestricted.setSummoningSick(false);

        List<Integer> result = ai.prepareAttackersForTax(gd, List.of(0, 2));

        assertThat(result).containsExactly(2);
    }

    @Test
    @DisplayName("Medium AI keeps one creature when every selected creature can only attack alone")
    void keepsOneWhenAllCanOnlyAttackAlone() {
        Permanent firstRestricted = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        firstRestricted.setSummoningSick(false);
        Permanent firstAura = harness.addToBattlefieldAndReturn(aiPlayer, new Errantry());
        firstAura.setAttachedTo(firstRestricted.getId());
        Permanent secondRestricted = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        secondRestricted.setSummoningSick(false);
        Permanent secondAura = harness.addToBattlefieldAndReturn(aiPlayer, new Errantry());
        secondAura.setAttachedTo(secondRestricted.getId());

        List<Integer> result = ai.prepareAttackersForTax(gd, List.of(0, 2));

        assertThat(result).containsExactly(0);
    }

    @Test
    @DisplayName("Medium AI ignores a stale attacker-declaration event")
    void ignoresStaleAttackerDeclarationEvent() {
        Permanent attacker = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        attacker.setSummoningSick(false);
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);
        TurnStep stepAfterDeclaration = gd.currentStep;
        int lifeAfterDeclaration = gd.getLife(human.getId());

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }
        assertThat(lifeAfterDeclaration).isEqualTo(18);
        assertThat(gd.currentStep).isEqualTo(stepAfterDeclaration);
        assertThat(gd.getLife(human.getId())).isEqualTo(lifeAfterDeclaration);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Medium AI ignores a blocker-declaration event for another player")
    void ignoresBlockerDeclarationForAnotherPlayer() {
        gd.interaction.beginInteraction(new PendingInteraction.BlockerDeclaration(human.getId()));
        AiGameActions actions = Mockito.mock(AiGameActions.class);
        MediumAiDecisionEngine engine = new MediumAiDecisionEngine(
                gd.id, aiPlayer, harness.getGameRegistry(), actions,
                harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(),
                harness.getTargetValidationService(), harness.getTargetLegalityService());

        engine.handleEvent(AiDecisionKind.BLOCKER_DECLARATION);

        verify(actions, never()).handleDeclareBlockers(any());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class).decidingPlayerId())
                .isEqualTo(human.getId());
    }

    @Test
    @DisplayName("Medium AI declares a blocker required to block if able")
    void honorsMustBlockIfAbleRequirement() {
        Permanent attacker = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setMustBlockThisTurnIfAble(true);

        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        ai.handleEvent(AiDecisionKind.BLOCKER_DECLARATION);

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Medium AI caps Lure blocks to an aura-granted maximum")
    void capsLureBlocksToAuraGrantedMaximum() {
        Permanent attacker = harness.addToBattlefieldAndReturn(human, new KjeldoranRoyalGuard());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent authority = harness.addToBattlefieldAndReturn(human, new AlphaAuthority());
        authority.setAttachedTo(attacker.getId());
        Permanent lure = harness.addToBattlefieldAndReturn(human, new Lure());
        lure.setAttachedTo(attacker.getId());
        Permanent firstBlocker = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        firstBlocker.setSummoningSick(false);
        Permanent secondBlocker = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        secondBlocker.setSummoningSick(false);

        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int attackerIndex = gd.playerBattlefields.get(human.getId()).indexOf(attacker);
        int firstBlockerIndex = gd.playerBattlefields.get(aiPlayer.getId()).indexOf(firstBlocker);
        int secondBlockerIndex = gd.playerBattlefields.get(aiPlayer.getId()).indexOf(secondBlocker);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            ai.sendBlockerDeclaration(new DeclareBlockersRequest(List.of(
                    new BlockerAssignment(firstBlockerIndex, attackerIndex),
                    new BlockerAssignment(secondBlockerIndex, attackerIndex))));
            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(List.of(firstBlocker, secondBlocker)).filteredOn(Permanent::isBlocking).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Medium AI drops a partial Tromokratis block without rejection")
    void dropsPartialTromokratisBlockWithoutRejection() {
        Permanent attacker = harness.addToBattlefieldAndReturn(human, new Tromokratis());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent firstBlocker = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        firstBlocker.setSummoningSick(false);
        Permanent secondBlocker = harness.addToBattlefieldAndReturn(aiPlayer, new HillGiant());
        secondBlocker.setSummoningSick(false);

        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int firstBlockerIndex = gd.playerBattlefields.get(aiPlayer.getId()).indexOf(firstBlocker);
        int attackerIndex = gd.playerBattlefields.get(human.getId()).indexOf(attacker);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            ai.sendBlockerDeclaration(new DeclareBlockersRequest(List.of(
                    new BlockerAssignment(firstBlockerIndex, attackerIndex))));
            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(firstBlocker.isBlocking()).isFalse();
        assertThat(secondBlocker.isBlocking()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Medium AI drops Okk when its greater-power partner is unaffordable")
    void dropsOkkWhenGreaterPowerPartnerIsUnaffordable() {
        Permanent attacker = harness.addToBattlefieldAndReturn(human, new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent okk = harness.addToBattlefieldAndReturn(aiPlayer, new Okk());
        okk.setSummoningSick(false);
        TestCards.mutableCard(okk).setPower(2);
        Permanent partner = harness.addToBattlefieldAndReturn(aiPlayer, new Hipparion());
        partner.setSummoningSick(false);
        TestCards.mutableCard(partner).setPower(4);

        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int attackerIndex = gd.playerBattlefields.get(human.getId()).indexOf(attacker);
        int okkIndex = gd.playerBattlefields.get(aiPlayer.getId()).indexOf(okk);
        int partnerIndex = gd.playerBattlefields.get(aiPlayer.getId()).indexOf(partner);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            ai.sendBlockerDeclaration(new DeclareBlockersRequest(List.of(
                    new BlockerAssignment(okkIndex, attackerIndex),
                    new BlockerAssignment(partnerIndex, attackerIndex))));
            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(okk.isBlocking()).isFalse();
        assertThat(partner.isBlocking()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Medium AI does not submit an unaffordable block tax from an attacking Archangel")
    void dropsUnaffordableGlobalBlockTax() {
        Permanent archangel = harness.addToBattlefieldAndReturn(human, new ArchangelOfTithes());
        archangel.setSummoningSick(false);
        archangel.setAttacking(true);
        Permanent attacker = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(aiPlayer, new HillGiant());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            ai.sendBlockerDeclaration(new DeclareBlockersRequest(List.of(new BlockerAssignment(
                    gd.playerBattlefields.get(aiPlayer.getId()).indexOf(blocker),
                    gd.playerBattlefields.get(human.getId()).indexOf(attacker)))));

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Medium AI casts higher-value spell when multiple available")
    void castsHigherValueSpell() {
        giveAiPriority();
        giveAiPlains(2);

        // Opponent has a big creature (Pacifism will be high value)
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        // Hand has Bears (creature value) and Pacifism (high value due to target)
        harness.setHand(aiPlayer, List.of(new GrizzlyBears(), new Pacifism()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should cast the spell with higher evaluated value
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Creature mana restriction =====

    @Test
    @DisplayName("Medium AI does not cast Myr Superion with only land mana")
    void doesNotCastMyrSuperionWithLandMana() {
        giveAiPriority();
        giveAiPlains(2);

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.m.MyrSuperion()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Myr Superion should NOT be on the stack A?€�t only land mana is available
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Medium AI casts Myr Superion when creature mana dorks are available")
    void castsMyrSuperionWithCreatureMana() {
        giveAiPriority();

        // Add two Llanowar Elves (creature mana dorks) to battlefield
        Permanent elf1 = new Permanent(new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        elf1.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(elf1);

        Permanent elf2 = new Permanent(new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        elf2.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(elf2);

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.m.MyrSuperion()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Myr Superion should be on the stack A?€�t creature mana is available from elves
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Myr Superion");
    }

    // ===== Sacrifice cost checks =====

    @Test
    @DisplayName("Medium AI skips spell with a sacrifice-an-artifact cost when no artifact on battlefield")
    void skipsSpellWithSacrificeArtifactCostWhenNoArtifact() {
        giveAiPriority();

        Permanent mountain = new Permanent(new Mountain());
        mountain.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(mountain);

        harness.setHand(aiPlayer, List.of(new KuldothaRebirth()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // AI should not cast A?€�t no artifact to sacrifice
        assertThat(gd.stack).isEmpty();
    }

    // ===== Sacrifice cost spell casting =====

    @Test
    @DisplayName("Medium AI casts Vivisection by sacrificing weakest creature")
    void castsVivisectionSacrificingWeakestCreature() {
        giveAiPriority();

        for (int i = 0; i < 4; i++) {
            Permanent island = new Permanent(new Island());
            island.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(island);
        }

        Permanent elves = new Permanent(new LlanowarElves()); // 1/1 A?€�t should be sacrificed
        elves.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(elves);

        Permanent angel = new Permanent(new SerraAngel()); // 4/4 A?€�t should survive
        angel.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(angel);

        harness.setHand(aiPlayer, List.of(new Vivisection()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vivisection");
        harness.assertNotOnBattlefield(aiPlayer, "Llanowar Elves");
        harness.assertOnBattlefield(aiPlayer, "Serra Angel");
    }

    // ===== Mindslaver-controlled turn =====

    /**
     * Hands the AI seat control of the opponent's turn through a real Mindslaver activation and
     * stops in that turn's declare-attackers step, returning the controlled player's must-attack
     * creature.
     *
     * <p>The engine addresses the attacker decision to the controller while the interaction stays
     * owned by the controlled player, so the AI has to declare from a battlefield that is not its
     * own. The controlled player is deliberately given more permanents than the AI seat and the
     * must-attack creature is put last, so an engine reading its own battlefield can only produce
     * indices that name something else — or nothing at all.
     */
    private Permanent mindslavedAttackerDeclaration() {
        Set<TurnStep> mainPhaseStops = Set.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN);
        gd.playerAutoStopSteps.put(human.getId(), ConcurrentHashMap.newKeySet());
        gd.playerAutoStopSteps.get(human.getId()).addAll(mainPhaseStops);
        gd.playerAutoStopSteps.put(aiPlayer.getId(), ConcurrentHashMap.newKeySet());
        gd.playerAutoStopSteps.get(aiPlayer.getId()).addAll(mainPhaseStops);

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(aiPlayer, new Mindslaver());
        harness.addMana(aiPlayer, ManaColor.COLORLESS, 4);
        harness.activateAbility(aiPlayer, 0, null, human.getId());
        harness.passBothPriorities();

        // Roll into the controlled player's turn, where the control actually takes effect.
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
        assertThat(gd.activePlayerId).isEqualTo(human.getId());
        assertThat(gd.mindControlledPlayerId).isEqualTo(human.getId());
        assertThat(gd.mindControllerPlayerId).isEqualTo(aiPlayer.getId());

        Permanent ownBears = new Permanent(new GrizzlyBears());
        ownBears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(ownBears);

        for (int i = 0; i < 3; i++) {
            Permanent forest = new Permanent(new Forest());
            forest.setSummoningSick(false);
            gd.playerBattlefields.get(human.getId()).add(forest);
        }
        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(berserkers);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.stack.clear();
        harness.beginAttackerDeclarationInput();
        return berserkers;
    }

    @Test
    @DisplayName("Medium AI attacks from the Mindslavered player's battlefield, not its own")
    void declaresAttackersForMindslaveredPlayer() {
        Permanent berserkers = mindslavedAttackerDeclaration();

        ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        assertThat(berserkers.isAttacking()).isTrue();
        assertThat(gd.playerBattlefields.get(aiPlayer.getId())).noneMatch(Permanent::isAttacking);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    // ===== Must-attack =====

    @Test
    @DisplayName("Medium AI includes must-attack creature even into unfavorable board")
    void includesMustAttackCreature() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        harness.beginAttackerDeclarationInput();

        // AI has Berserkers of Blood Ridge (4/4 must-attack)
        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(berserkers);

        // Opponent has Air Elemental (4/4 flying) A?€�t can block
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        // Berserkers must be attacking despite the unfavorable board
        assertThat(berserkers.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Medium AI includes must-attack creature alongside optional creatures")
    void includesMustAttackWithOptional() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        harness.beginAttackerDeclarationInput();
        gd.playerLifeTotals.put(human.getId(), 20);

        // AI has Berserkers (4/4 must-attack) and Bears (2/2 optional)
        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(berserkers);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears);

        // No blockers A?€�t both should attack, dealing at least 4 damage (must-attack Berserkers)
        ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        // Berserkers (4 power) must have attacked; combat fully resolves with no blockers
        assertThat(gd.playerLifeTotals.get(human.getId())).isLessThanOrEqualTo(16);
    }

    // ===== Mana tapping before spell casting =====

    @Test
    @DisplayName("Medium AI taps lands before casting sorcery-speed spell")
    void tapsLandsBeforeCastingSorcery() {
        giveAiPriority();

        // Use Forests so GrizzlyBears ({1}{G}) is castable
        Permanent forest1 = new Permanent(new com.github.laxika.magicalvibes.cards.f.Forest());
        forest1.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(forest1);

        Permanent forest2 = new Permanent(new com.github.laxika.magicalvibes.cards.f.Forest());
        forest2.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(forest2);

        // Mana pool is empty A?€�t AI must tap Forests
        assertThat(gd.playerManaPools.get(aiPlayer.getId()).getTotal()).isZero();

        harness.setHand(aiPlayer, List.of(new GrizzlyBears()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");

        // Forests should be tapped
        long tappedCount = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isTapped)
                .count();
        assertThat(tappedCount).isEqualTo(2);
    }

    // ===== tryCastSpell silent failure recovery =====

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("tryCastSpell silent failure recovery")
    class TryCastSpellSilentFailureRecovery {

        @Mock private AiGameActions mockMessageHandler;
        @Mock private GameQueryService mockGameQueryService;
        @Mock private BlockLegalityService mockBlockLegalityService;
        @Mock private CombatAttackService mockCombatAttackService;
        @Mock private GameActionAvailabilityService mockGameActionAvailabilityService;
        @Mock private com.github.laxika.magicalvibes.service.cast.CastingCostService mockCastingCostService;
        @Mock private com.github.laxika.magicalvibes.service.cast.CastingPermissionService mockCastingPermissionService;
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

        private MediumAiDecisionEngine createEngine() {
            AiTestPlayabilityStub.install(mockGameActionAvailabilityService, mockCastingCostService, mockGameQueryService);
            MediumAiDecisionEngine engine = new MediumAiDecisionEngine(
                    mockGd.id, mockAiPlayer, mockGameRegistry, mockMessageHandler,
                    mockGameQueryService, mockBlockLegalityService, mockCombatAttackService, mockGameActionAvailabilityService,
                    mockCastingCostService, mockCastingPermissionService,
                    mockTargetValidationService,
                    new com.github.laxika.magicalvibes.service.target.TargetLegalityService(mockGameQueryService,
                            new com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService(mockGameQueryService),
                            mockTargetValidationService,
                            org.mockito.Mockito.mock(com.github.laxika.magicalvibes.service.effect.AmountEvaluationService.class),
                            new com.github.laxika.magicalvibes.service.target.TargetGroupAssignmentService(mockGameQueryService)));
            return engine;
        }

        @Test
        @DisplayName("Medium AI passes priority when spell cast is silently rejected")
        void passesPriorityWhenSpellCastSilentlyRejected() throws Exception {
            Card creature = new Card();
            creature.setName("Test Bear");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{1}{G}");
            creature.setPower(2);
            creature.setToughness(2);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(creature);

            ManaPool pool = mockGd.playerManaPools.get(mockAiPlayer.getId());
            pool.add(ManaColor.GREEN, 1);
            pool.add(ManaColor.COLORLESS, 1);

            createEngine().handleEvent(AiDecisionKind.GAME_STATE);

            verify(mockMessageHandler).handlePlayCard(any());
            verify(mockMessageHandler).handlePassPriority(any());
        }

        @Test
        @DisplayName("Medium AI does NOT pass priority when spell cast succeeds")
        void doesNotPassPriorityWhenSpellCastSucceeds() throws Exception {
            Card creature = new Card();
            creature.setName("Test Bear");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{1}{G}");
            creature.setPower(2);
            creature.setToughness(2);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(creature);

            ManaPool pool = mockGd.playerManaPools.get(mockAiPlayer.getId());
            pool.add(ManaColor.GREEN, 1);
            pool.add(ManaColor.COLORLESS, 1);

            Mockito.doAnswer(inv -> {
                mockGd.playerHands.get(mockAiPlayer.getId()).removeFirst();
                return null;
            }).when(mockMessageHandler).handlePlayCard(any());

            createEngine().handleEvent(AiDecisionKind.GAME_STATE);

            verify(mockMessageHandler).handlePlayCard(any());
            verify(mockMessageHandler, never()).handlePassPriority(any());
        }

        // ===== Identity-based cast detection (explore-refill regression) =====

        @Test
        @DisplayName("Medium AI detects cast success when ETB refills hand with a land (e.g. Explore)")
        void detectsCastSuccessWhenEtbRefillsHandWithLand() throws Exception {
            // Regression: Queen's Agent ETB triggers Explore which can refill hand with a land,
            // leaving hand.size() unchanged. The fix uses identity (hand.contains) not size.
            Card creature = new Card();
            creature.setName("Queen's Agent");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{5}{B}");
            creature.setPower(3);
            creature.setToughness(3);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(creature);

            ManaPool pool = mockGd.playerManaPools.get(mockAiPlayer.getId());
            pool.add(ManaColor.BLACK, 1);
            pool.add(ManaColor.COLORLESS, 5);

            Mockito.doAnswer(inv -> {
                List<Card> hand = mockGd.playerHands.get(mockAiPlayer.getId());
                hand.remove(creature);
                Card revealedLand = new Card();
                revealedLand.setName("Forest");
                revealedLand.setType(CardType.LAND);
                hand.add(revealedLand);
                return null;
            }).when(mockMessageHandler).handlePlayCard(any());

            createEngine().handleEvent(AiDecisionKind.GAME_STATE);

            verify(mockMessageHandler).handlePlayCard(any());
            verify(mockMessageHandler, never()).handlePassPriority(any());
        }

        @Test
        @DisplayName("Medium AI still detects genuine silent failure when hand has other cards")
        void detectsGenuineFailureWhenHandHasOtherCards() throws Exception {
            Card creature = new Card();
            creature.setName("Test Bear");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{1}{G}");
            creature.setPower(2);
            creature.setToughness(2);
            Card sibling = new Card();
            sibling.setName("Other Card");
            sibling.setType(CardType.SORCERY);
            sibling.setManaCost("{10}{U}{U}");
            mockGd.playerHands.get(mockAiPlayer.getId()).add(creature);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(sibling);

            ManaPool pool = mockGd.playerManaPools.get(mockAiPlayer.getId());
            pool.add(ManaColor.GREEN, 1);
            pool.add(ManaColor.COLORLESS, 1);

            createEngine().handleEvent(AiDecisionKind.GAME_STATE);

            verify(mockMessageHandler).handlePlayCard(any());
            verify(mockMessageHandler).handlePassPriority(any());
        }

        @Test
        @DisplayName("Medium AI does not throw when ETB refills hand with a null-cost land")
        void noExceptionWhenEtbRefillsHandWithNullCostLand() throws Exception {
            Card creature = new Card();
            creature.setName("Queen's Agent");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{5}{B}");
            creature.setPower(3);
            creature.setToughness(3);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(creature);

            ManaPool pool = mockGd.playerManaPools.get(mockAiPlayer.getId());
            pool.add(ManaColor.BLACK, 1);
            pool.add(ManaColor.COLORLESS, 5);

            Mockito.doAnswer(inv -> {
                List<Card> hand = mockGd.playerHands.get(mockAiPlayer.getId());
                hand.remove(creature);
                Card revealedLand = new Card();
                revealedLand.setName("Forest");
                revealedLand.setType(CardType.LAND);
                hand.add(revealedLand);
                return null;
            }).when(mockMessageHandler).handlePlayCard(any());

            assertThatCode(() -> createEngine().handleEvent(AiDecisionKind.GAME_STATE))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Medium AI builds damage assignments for divided damage spell")
        void buildsDamageAssignmentsForDividedDamageSpell() throws Exception {
            Card spell = new Card();
            spell.setName("Test Divided Damage");
            spell.setType(CardType.SORCERY);
            spell.setManaCost("{1}{R}");
            spell.target(null, 1, 3)
                    .addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongTargetCreatures(3));
            mockGd.playerHands.get(mockAiPlayer.getId()).add(spell);

            ManaPool pool = mockGd.playerManaPools.get(mockAiPlayer.getId());
            pool.add(ManaColor.RED, 1);
            pool.add(ManaColor.COLORLESS, 1);

            UUID opponentId = mockGd.orderedPlayerIds.get(1);
            Card creatureCard = new Card();
            creatureCard.setName("Opponent Creature");
            creatureCard.setType(CardType.CREATURE);
            creatureCard.setPower(2);
            creatureCard.setToughness(2);
            Permanent creature = new Permanent(creatureCard);
            mockGd.playerBattlefields.get(opponentId).add(creature);

            when(mockGameQueryService.isCreature(mockGd, creature)).thenReturn(true);
            when(mockGameQueryService.getEffectiveToughness(mockGd, creature)).thenReturn(2);
            when(mockTargetValidationService.checkEffectTargets(any(), any())).thenReturn(Optional.empty());

            Mockito.doAnswer(inv -> {
                mockGd.playerHands.get(mockAiPlayer.getId()).removeFirst();
                return null;
            }).when(mockMessageHandler).handlePlayCard(any());

            createEngine().handleEvent(AiDecisionKind.GAME_STATE);

            ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
            verify(mockMessageHandler).handlePlayCard(captor.capture());

            PlayCardRequest request = captor.getValue();
            assertThat(request.damageAssignments()).isNotNull();
            assertThat(request.damageAssignments()).containsEntry(creature.getId(), 3);
        }

        @Test
        @DisplayName("Medium AI does not cast spell when mana tapping triggers awaiting input")
        void doesNotCastSpellWhenManaTappingTriggersAwaitingInput() throws Exception {
            Card creature = new Card();
            creature.setName("Test Knight");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{W}");
            creature.setPower(2);
            creature.setToughness(2);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(creature);

            // Add an untapped Plains so AI needs to tap it for mana
            Permanent land = new Permanent(new Plains());
            land.setSummoningSick(false);
            mockGd.playerBattlefields.get(mockAiPlayer.getId()).add(land);

            // Allow tapping flow to proceed
            when(mockGameQueryService.canActivateManaAbility(any(), any())).thenReturn(true);

            // Simulate mana ability triggering awaiting input (e.g. Treasure color choice)
            Mockito.doAnswer(inv -> {
                mockGd.interaction.beginInteraction(new PendingInteraction.ColorChoice(null, null, null, null, java.util.List.of(), "Choose a color."));
                return null;
            }).when(mockMessageHandler).handleTapPermanent(any());

            createEngine().handleEvent(AiDecisionKind.GAME_STATE);

            // AI should have tapped but NOT cast the spell or passed priority
            verify(mockMessageHandler).handleTapPermanent(any());
            verify(mockMessageHandler, never()).handlePlayCard(any());
            verify(mockMessageHandler, never()).handlePassPriority(any());
        }

        @Test
        @DisplayName("Medium AI does not cast a spell when mana tapping puts a trigger on the stack")
        void doesNotCastSpellWhenManaTappingPutsTriggerOnStack() throws Exception {
            Card creature = new Card();
            creature.setName("Test Knight");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{W}");
            creature.setPower(2);
            creature.setToughness(2);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(creature);

            Permanent land = new Permanent(new Plains());
            land.setSummoningSick(false);
            mockGd.playerBattlefields.get(mockAiPlayer.getId()).add(land);

            when(mockGameQueryService.canActivateManaAbility(any(), any())).thenReturn(true);
            Mockito.doAnswer(inv -> {
                mockGd.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature,
                        mockAiPlayer.getId(), "Test trigger", List.of()));
                return null;
            }).when(mockMessageHandler).handleTapPermanent(any());

            createEngine().handleEvent(AiDecisionKind.GAME_STATE);

            verify(mockMessageHandler).handleTapPermanent(any());
            verify(mockMessageHandler, never()).handlePlayCard(any());
            verify(mockMessageHandler, never()).handlePassPriority(any());
        }
    }

    // ===== Modal spell handling (ChooseOneEffect) =====

    @Test
    @DisplayName("Medium AI chooses Wear // Tear's affordable mode")
    void choosesAffordableWearTearMode() {
        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(human.getId());
        giveAiPlains(1);

        Permanent artifact = harness.addToBattlefieldAndReturn(human, new Ornithopter());
        Permanent enchantment = harness.addToBattlefieldAndReturn(human, new AngelicChorus());
        harness.setHand(aiPlayer, List.of(new WearTear()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Wear");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enchantment.getId());
        assertThat(gd.playerBattlefields.get(aiPlayer.getId()))
                .filteredOn(Permanent::isTapped)
                .hasSize(1);
        assertThat(gd.playerManaPools.get(aiPlayer.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(aiPlayer.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerBattlefields.get(human.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly(artifact.getCard().getName(), enchantment.getCard().getName());
    }

    @Test
    @DisplayName("Medium AI casts Cryptic Command with its choose-two target")
    void castsCrypticCommandWithChooseTwoTarget() {
        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(human.getId());
        giveAiIslands(4);

        Permanent target = new Permanent(new AirElemental());
        target.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(target);
        harness.setHand(aiPlayer, List.of(new CrypticCommand()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Cryptic Command");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Medium AI validates modal player-or-planeswalker target candidates")
    void castsModalPlayerOrPlaneswalkerSpellAtPlayer() {
        giveAiPriority();
        giveAiMountains(1);
        giveAiPlains(1);
        Permanent ordinaryPermanent = harness.addToBattlefieldAndReturn(human, new GrizzlyBears());
        Card modalSpell = new Card();
        modalSpell.setName("Draw and Bolt");
        modalSpell.setType(CardType.SORCERY);
        modalSpell.setManaCost("{R}{W}");
        modalSpell.addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw and damage", List.of(
                        new DrawCardEffect(1), new DealDamageToTargetPlayerOrPlaneswalkerEffect(1))),
                new ChooseOneEffect.ChooseOneOption("Draw", new DrawCardEffect(1)))));
        harness.setHand(aiPlayer, List.of(modalSpell));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(human.getId());
        assertThat(gd.stack.getFirst().getTargetId()).isNotEqualTo(ordinaryPermanent.getId());
    }

    @Test
    @DisplayName("Medium AI supplies both targets for Blinding Beam's tap mode")
    void castsBlindingBeamWithTwoTargetCreatures() {
        giveAiPriority();
        giveAiPlains(3);

        Permanent firstTarget = new Permanent(new AirElemental());
        Permanent secondTarget = new Permanent(new AirElemental());
        gd.playerBattlefields.get(human.getId()).add(firstTarget);
        gd.playerBattlefields.get(human.getId()).add(secondTarget);
        harness.setHand(aiPlayer, List.of(new BlindingBeam()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Blinding Beam");
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
        assertThat(gd.stack.getFirst().getTargetIds())
                .containsExactlyInAnyOrder(firstTarget.getId(), secondTarget.getId());
    }

    @Test
    @DisplayName("Medium AI allows one permanent for both Reign of Chaos targets")
    void castsReignOfChaosWithSharedTarget() {
        giveAiPriority();
        giveAiMountains(4);

        Permanent target = new Permanent(whitePlainsCreature());
        gd.playerBattlefields.get(human.getId()).add(target);
        harness.setHand(aiPlayer, List.of(new ReignOfChaos()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Reign of Chaos");
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
        assertThat(gd.stack.getFirst().getTargetIds())
                .containsExactly(target.getId(), target.getId());
    }

    @Test
    @DisplayName("Medium AI casts Pyrrhic Strike's single mode without paying blight")
    void castsSingleModeWithoutPayingOptionalBlight() {
        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(human.getId());
        giveAiPlains(3);
        Permanent blightCreature = harness.addToBattlefieldAndReturn(aiPlayer, new HillGiant());
        Permanent artifact = harness.addToBattlefieldAndReturn(human, new Ornithopter());
        harness.setHand(aiPlayer, List.of(new PyrrhicStrike()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds()).containsExactly(artifact.getId());
        assertThat(blightCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Medium AI does not cast Steel Sabotage when no mode has valid targets")
    void doesNotCastSteelSabotageWhenNoValidMode() {
        giveAiPriority();
        giveAiIslands(1);

        harness.setHand(aiPlayer, List.of(new SteelSabotage()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Medium AI does not cast Unbury when neither mode has a legal graveyard target")
    void doesNotCastUnburyWithoutCreatureInGraveyard() {
        giveAiPriority();
        giveAiSwamps(2);
        gd.playerGraveyards.get(aiPlayer.getId()).add(new HolyDay());
        harness.setHand(aiPlayer, List.of(new Unbury()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(aiPlayer.getId()))
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerHands.get(aiPlayer.getId())).singleElement()
                .isInstanceOf(Unbury.class);
    }

    @Test
    @DisplayName("Medium AI does not cast Victimize without two creature cards in its graveyard")
    void doesNotCastVictimizeWithoutTwoCreatureCardsInGraveyard() {
        giveAiPriority();
        giveAiSwamps(3);
        gd.playerGraveyards.get(aiPlayer.getId()).add(new HolyDay());
        Victimize victimize = new Victimize();
        harness.setHand(aiPlayer, List.of(victimize));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(aiPlayer.getId()))
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerHands.get(aiPlayer.getId())).containsExactly(victimize);
    }

    @Test
    @DisplayName("Medium AI selects Torrent of Souls' optional graveyard and player targets separately")
    void selectsTorrentOfSoulsTargetsSeparately() {
        giveAiPriority();
        TorrentOfSouls torrentOfSouls = new TorrentOfSouls();

        AiTargetSelector.SpellTargetSelection selection = ai.targetSelector
                .chooseSeparateGraveyardTargets(gd, torrentOfSouls, aiPlayer.getId());

        assertThat(selection).isNotNull();
        assertThat(selection.targetId()).isNull();
        assertThat(selection.targetIds()).containsExactly(human.getId());
    }

    @Test
    @DisplayName("Medium AI skips Steel Sabotage (no valid mode) and casts another available spell")
    void skipsModalSpellAndCastsAlternative() {
        giveAiPriority();
        giveAiPlains(2);

        // Opponent has a creature (Pacifism will be high value)
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        // Steel Sabotage has no valid mode (no artifacts), but Pacifism is castable
        harness.setHand(aiPlayer, List.of(new SteelSabotage(), new Pacifism()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should skip Steel Sabotage and cast Pacifism
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pacifism");
    }

    @Test
    @DisplayName("Medium AI casts Steel Sabotage to bounce artifact creature on opponent's battlefield")
    void castsSteelSabotageToBounceArtifact() {
        // Set up as opponent's turn, beginning of combat A?€�t good timing for REMOVAL instants
        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(human.getId());

        giveAiIslands(1);

        // Artifact creature so bounce evaluator gives positive value (creature score)
        Card artifactCreature = new Card();
        artifactCreature.setName("Test Artifact Creature");
        artifactCreature.setType(CardType.ARTIFACT);
        artifactCreature.setAdditionalTypes(Set.of(CardType.CREATURE));
        artifactCreature.setPower(3);
        artifactCreature.setToughness(3);
        Permanent artifactPerm = new Permanent(artifactCreature);
        gd.playerBattlefields.get(human.getId()).add(artifactPerm);

        harness.setHand(aiPlayer, List.of(new SteelSabotage()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Steel Sabotage");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(artifactPerm.getId());
    }

    @Test
    @DisplayName("Medium AI casts Slagstorm to wipe opponent's creatures")
    void castsSlagstorm() {
        giveAiPriority();
        giveAiMountains(3);

        // Opponent has creatures so board wipe evaluator gives positive value
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new Slagstorm()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Slagstorm");
    }

    // ===== Attack tax handling =====

    @Test
    @DisplayName("Medium AI limits attackers when attack tax is present")
    void limitsAttackersWhenAttackTaxPresent() {
        // Human controls Baird (tax {1} per attacker)
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
        gd.status = GameStatus.RUNNING;
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(aiPlayer.getId()));

        ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        // At most 1 creature should be attacking (can only afford {1} tax)
        long attackingCount = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isAttacking)
                .count();
        assertThat(attackingCount).isLessThanOrEqualTo(1);
    }

    // ===== ExileNCardsFromGraveyardCost (e.g. Skaab Ruinator) =====

    @Test
    @DisplayName("Medium AI casts Skaab Ruinator when graveyard has 3 creature cards")
    void castsSkaabRuinatorWithThreeCreatures() {
        giveAiPriority();
        giveAiIslands(3); // Skaab Ruinator costs {1}{U}{U}

        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.s.SkaabRuinator()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skaab Ruinator");
        assertThat(gd.getPlayerExiledCards(aiPlayer.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(aiPlayer.getId())).isEmpty();
    }

    @Test
    @DisplayName("Medium AI casts Skaab Ruinator selecting only creatures from mixed graveyard")
    void castsSkaabRuinatorFromMixedGraveyard() {
        giveAiPriority();
        giveAiIslands(3);

        gd.playerGraveyards.get(aiPlayer.getId()).add(new com.github.laxika.magicalvibes.cards.h.HolyDay());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new com.github.laxika.magicalvibes.cards.h.HolyDay());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.s.SkaabRuinator()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skaab Ruinator");
        assertThat(gd.getPlayerExiledCards(aiPlayer.getId())).hasSize(3);
        // Only creatures exiled A?€�t 2 non-creatures remain
        assertThat(gd.playerGraveyards.get(aiPlayer.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(aiPlayer.getId()))
                .allMatch(c -> c.getName().equals("Holy Day"));
    }

    @Test
    @DisplayName("Medium AI exiles only cards matching an ExileX graveyard cost")
    void castsExileXCostFromMixedGraveyard() {
        giveAiPriority();

        Card spell = new Card();
        spell.setName("Mixed Graveyard Spell");
        spell.setType(CardType.SORCERY);
        spell.setManaCost("{B}");
        spell.addEffect(EffectSlot.SPELL,
                new com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost(CardType.CREATURE));
        spell.addEffect(EffectSlot.SPELL, new com.github.laxika.magicalvibes.model.effect.DrawCardEffect(1));

        harness.setGraveyard(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.h.HolyDay(),
                new GrizzlyBears(), new com.github.laxika.magicalvibes.cards.h.HolyDay()));
        harness.setHand(aiPlayer, List.of(spell));
        harness.addMana(aiPlayer, ManaColor.BLACK, 1);

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(aiPlayer.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerGraveyards.get(aiPlayer.getId()))
                .extracting(Card::getName)
                .containsExactly("Holy Day", "Holy Day");
    }

    // ===== Entrancing Melody (PermanentManaValueEqualsXPredicate) =====

    @Test
    @DisplayName("Medium AI casts Entrancing Melody with X matching target creature's mana value")
    void castsEntrancingMelodyWithCorrectX() {
        giveAiPriority();
        giveAiIslands(4); // maxX = 4 - 2 (for {U}{U}) = 2

        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new EntrancingMelody()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Entrancing Melody");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Medium AI casts Dominate with a target within the announced X")
    void castsDominateWithTargetWithinAnnouncedX() {
        giveAiPriority();
        giveAiIslands(6); // maxX = 3; the target determines X=2

        Permanent tooExpensive = new Permanent(new HillGiant()); // MV=4
        tooExpensive.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(tooExpensive);

        Permanent target = new Permanent(new GrizzlyBears()); // MV=2
        target.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(target);

        Dominate dominate = new Dominate();
        harness.setHand(aiPlayer, List.of(dominate));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(dominate);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
        assertThat(gd.stack.getFirst().getTargetId()).isNotEqualTo(tooExpensive.getId());
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Medium AI picks highest affordable mana value target for Entrancing Melody")
    void picksHighestAffordableTargetForEntrancingMelody() {
        giveAiPriority();
        giveAiIslands(4); // maxX = 2

        Permanent vanguard = new Permanent(new EliteVanguard()); // MV=1
        vanguard.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(vanguard);

        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new EntrancingMelody()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Medium AI skips Entrancing Melody when target too expensive")
    void skipsEntrancingMelodyWhenTooExpensive() {
        giveAiPriority();
        giveAiIslands(3); // maxX = 1

        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2, unaffordable
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new EntrancingMelody()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Medium AI pays an X-discard additional cost")
    void castsXDiscardSpellWithRequiredDiscardCard() {
        giveAiPriority();
        giveAiIslands(3);
        AetherTide aetherTide = new AetherTide();
        GrizzlyBears discard = new GrizzlyBears();
        harness.addToBattlefield(human, new GrizzlyBears());
        harness.setHand(aiPlayer, List.of(aetherTide, discard));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(aetherTide);
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(1);
        assertThat(gd.playerHands.get(aiPlayer.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(aiPlayer.getId()))
                .containsExactly(discard);
    }

    @Test
    @DisplayName("Medium AI supplies all cards for a fixed multi-card discard cost")
    void castsCatharticReunionWithTwoDiscardCards() {
        giveAiPriority();
        giveAiMountains(2);
        CatharticReunion reunion = new CatharticReunion();
        GrizzlyBears firstDiscard = new GrizzlyBears();
        GrizzlyBears secondDiscard = new GrizzlyBears();
        GrizzlyBears remainingCard = new GrizzlyBears();
        harness.setHand(aiPlayer, List.of(reunion, firstDiscard, secondDiscard, remainingCard));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(reunion);
        assertThat(gd.playerGraveyards.get(aiPlayer.getId()))
                .containsExactlyInAnyOrder(firstDiscard, secondDiscard);
        assertThat(gd.playerHands.get(aiPlayer.getId())).containsExactly(remainingCard);
    }

    // ===== X value cap handling =====

    @Test
    @DisplayName("Medium AI respects a binding X value cap instead of attempting an illegal cast")
    void respectsBindingXValueCap() {
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            giveAiPriority();
            giveAiIslands(4); // 4U total; {X}{U}{U} -> maxX = 2 on mana alone

            Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
            bears.setSummoningSick(false);
            gd.playerBattlefields.get(human.getId()).add(bears);

            // Hypothetical "X can't be greater than the number of snow lands you control" (the
            // shape Winter's Chill uses). The AI controls plain Islands, so the cap is 0.
            EntrancingMelody capped = new EntrancingMelody();
            capped.setXValueCap(new PermanentCount(
                    new PermanentHasSupertypePredicate(CardSupertype.SNOW), CountScope.CONTROLLER));
            harness.setHand(aiPlayer, List.of(capped));

            ai.handleEvent(AiDecisionKind.GAME_STATE);

            // The spell is uncastable either way, so an empty stack proves nothing on its own.
            // What the clamp changes is whether the AI ANNOUNCES an illegal X=2 and has
            // SpellCastingService reject it ("X can't be greater than 0"), burning its priority.
            // That rejection surfaces as a "PlayCard failed silently" legality disagreement.
            assertThat(watcher.drainFailures()).isEmpty();
            assertThat(gd.stack).isEmpty();
        } finally {
            watcher.uninstall();
        }
    }

    @Test
    @DisplayName("Medium AI still casts when the X value cap is not binding")
    void castsWhenXValueCapNotBinding() {
        giveAiPriority();
        giveAiIslands(4); // maxX = 2

        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        // Cap counts lands you control (4) — above the affordable X, so it must not restrict.
        EntrancingMelody capped = new EntrancingMelody();
        capped.setXValueCap(new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER));
        harness.setHand(aiPlayer, List.of(capped));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    // ===== X-spell cost modifier handling =====

    @Test
    @DisplayName("Medium AI skips Entrancing Melody when cost modifier makes only target unaffordable")
    void skipsEntrancingMelodyWhenCostModifierMakesTargetUnaffordable() {
        giveAiPriority();
        giveAiIslands(4); // 4U total; Entrancing Melody {X}{U}{U} A?†’ without modifier maxX=2

        // Thalia on opponent's battlefield: +1 cost A?†’ maxX=1
        Permanent thalia = new Permanent(new com.github.laxika.magicalvibes.cards.t.ThaliaGuardianOfThraben());
        thalia.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(thalia);

        // MV=2 creature A?€�t needs X=2 but maxX=1 with Thalia A?†’ unaffordable
        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new EntrancingMelody()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Without the fix, AI would compute maxX=2 (ignoring modifier) and try to steal Bears,
        // which would fail server-side validation. With the fix, AI sees maxX=1 and skips.
        assertThat(gd.stack).isEmpty();
    }

    // ===== Forced attack (Trove of Temptation) =====

    @Test
    @DisplayName("Medium AI chooses a legal attacker when forced and the first candidate is restricted")
    void attacksWithAtLeastOneWhenForcedByTroveOfTemptation() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        harness.beginAttackerDeclarationInput();

        // Opponent controls Trove of Temptation
        Permanent trove = new Permanent(new TroveOfTemptation());
        trove.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(trove);

        Permanent conscripts = new Permanent(new OrcishConscripts());
        conscripts.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(conscripts);

        // AI has a 2/2 creature and opponent has a 4/4 A?€�t simulator would normally skip attacking
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears);

        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        // AI must attack with at least one creature despite the unfavorable board
        long attackingCount = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isAttacking)
                .count();
        assertThat(attackingCount).isGreaterThanOrEqualTo(1);
        assertThat(conscripts.isAttacking()).isFalse();
        assertThat(bears.isAttacking()).isTrue();
    }

    // ===== Targeting tax handling =====

    @Test
    @DisplayName("Medium AI does not cast Pacifism when targeting tax makes it unaffordable")
    void doesNotCastPacifismWhenTargetingTaxMakesUnaffordable() {
        giveAiPriority();
        giveAiPlains(2); // Only 2 mana A?€�t Pacifism costs {1}{W} but Kopala adds {2}

        Permanent kopala = new Permanent(new com.github.laxika.magicalvibes.cards.k.KopalaWardenOfWaves());
        kopala.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(kopala);

        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should NOT cast A?€�t can't afford {1}{W} + {2} tax = 4 mana with only 2 Plains
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(aiPlayer.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .allMatch(permanent -> !permanent.isTapped());
    }

    @Test
    @DisplayName("Medium AI casts Pacifism when it can afford targeting tax")
    void castsPacifismWhenCanAffordTargetingTax() {
        giveAiPriority();
        giveAiPlains(4); // 4 mana A?€�t enough for {1}{W} + {2} tax

        Permanent kopala = new Permanent(new com.github.laxika.magicalvibes.cards.k.KopalaWardenOfWaves());
        kopala.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(kopala);

        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pacifism");
        assertThat(gd.playerBattlefields.get(aiPlayer.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Medium AI does not cast instant when targeting tax makes it unaffordable")
    void doesNotCastInstantWhenTargetingTaxMakesUnaffordable() {
        // Set up as opponent's turn, beginning of combat A?€�t good timing for REMOVAL instants
        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(human.getId());

        giveAiMountains(1); // Only 1 mana A?€�t Lightning Bolt costs {R} but Kopala adds {2}

        Permanent kopala = new Permanent(new com.github.laxika.magicalvibes.cards.k.KopalaWardenOfWaves());
        kopala.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(kopala);

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.l.LightningBolt()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should NOT cast A?€�t can't afford {R} + {2} tax = 3 mana with only 1 Mountain
        assertThat(gd.stack).isEmpty();
    }

}
