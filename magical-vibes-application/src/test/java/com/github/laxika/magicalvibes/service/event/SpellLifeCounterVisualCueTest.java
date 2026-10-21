package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.cards.a.AtarkaEfreet;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.d.DragonscaleBoon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.ManaLeak;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.SignInBlood;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.event.VisualCueType;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.TestGameEventSubscriber;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Stack, life and counter cues: {@link VisualCueType#SPELL_CAST}, {@link VisualCueType#SPELL_RESOLVED},
 * {@link VisualCueType#SPELL_COUNTERED}, {@link VisualCueType#LIFE_GAINED},
 * {@link VisualCueType#LIFE_LOST} and {@link VisualCueType#COUNTERS_ADDED}.
 */
@CardUsed({LightningBolt.class, GrizzlyBears.class, Counterspell.class, ManaLeak.class, HealingSalve.class,
        SignInBlood.class, DragonscaleBoon.class, ProdigalPyromancer.class, AtarkaEfreet.class})
class SpellLifeCounterVisualCueTest extends BaseCardTest {

    private static final ObjectMapper JSON = new JacksonConfig().objectMapper();

    private final List<GameEventBatch> batches = new ArrayList<>();
    private AutoCloseable subscription;

    @BeforeEach
    void subscribeToBatches() {
        gd.aiPlayerIds.clear();
        subscription = GameTestEngineContext.get().getBean(TestGameEventSubscriber.class).subscribe(batches::add);
    }

    @AfterEach
    void unsubscribe() throws Exception {
        subscription.close();
    }

    @Test
    @DisplayName("Casting Lightning Bolt at a creature emits a cast cue naming the spell and its target")
    void castCueNamesTheTargetPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());

        assertThat(cues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.SPELL_CAST);
            assertThat(cue.sourceCardId()).isEqualTo(bolt.getId());
            assertThat(cue.sourceName()).isEqualTo("Lightning Bolt");
            assertThat(cue.sourceColors()).containsExactly(CardColor.RED);
            assertThat(cue.targetPermanentId()).isEqualTo(bears.getId());
            assertThat(cue.targetPlayerId()).isNull();
            assertThat(cue.targetCardId()).isNull();
        });
    }

    @Test
    @DisplayName("Casting Lightning Bolt at a player emits a cast cue naming that player")
    void castCueNamesTheTargetPlayer() {
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(cues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.SPELL_CAST);
            assertThat(cue.targetPlayerId()).isEqualTo(player2.getId());
            assertThat(cue.targetPermanentId()).isNull();
        });
    }

    @Test
    @DisplayName("Casting Counterspell emits a cast cue naming the spell it targets, on the wire too")
    void castCueNamesTheTargetSpell() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        batches.clear();
        harness.getConn1().clearMessages();

        harness.castInstant(player2, 0, bolt.getId());

        assertThat(cues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.SPELL_CAST);
            assertThat(cue.sourceName()).isEqualTo("Counterspell");
            assertThat(cue.targetCardId()).isEqualTo(bolt.getId());
        });
        assertThat(projectedCues()).singleElement().satisfies(cue -> {
            assertThat(cue.get("type").asString()).isEqualTo("SPELL_CAST");
            assertThat(cue.get("targetCardId").asString()).isEqualTo(bolt.getId().toString());
            assertThat(cue.get("counterType").isNull()).isTrue();
        });
    }

    @Test
    @DisplayName("A face-down spell's cast cue keeps its card id but carries no name or colors")
    void faceDownCastCueIsAnonymous() {
        AtarkaEfreet efreet = new AtarkaEfreet();
        harness.setHand(player1, List.of(efreet));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);

        assertThat(cues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.SPELL_CAST);
            assertThat(cue.sourceCardId()).isEqualTo(efreet.getId());
            assertThat(cue.sourceName()).isNull();
            assertThat(cue.sourceColors()).isEmpty();
        });
    }

    @Test
    @DisplayName("A resolving instant emits its resolve cue before the damage it deals")
    void resolveCueComesBeforeTheSpellsEffects() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        batches.clear();

        harness.passBothPriorities();

        assertThat(cues()).extracting(GameEventFact.VisualCue::cueType)
                .containsExactly(VisualCueType.SPELL_RESOLVED, VisualCueType.DAMAGE_TO_PLAYER);
        assertThat(cues().getFirst().sourceCardId()).isEqualTo(bolt.getId());
        assertThat(cues().getFirst().sourceName()).isEqualTo("Lightning Bolt");
    }

    @Test
    @DisplayName("A resolving creature spell emits a resolve cue")
    void creatureSpellResolveCue() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        batches.clear();

        harness.passBothPriorities();

        assertThat(cues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.SPELL_RESOLVED);
            assertThat(cue.sourceCardId()).isEqualTo(bears.getId());
        });
    }

    @Test
    @DisplayName("A spell whose only target is gone doesn't resolve and emits no resolve cue")
    void fizzledSpellEmitsNoResolveCue() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        batches.clear();

        harness.passBothPriorities();

        assertThat(cues()).isEmpty();
    }

    @Test
    @DisplayName("A resolving activated ability is not a spell and emits no resolve cue")
    void abilityEmitsNoResolveCue() {
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.activateAbility(player1, 0, null, player2.getId());
        batches.clear();

        harness.passBothPriorities();

        assertThat(cues()).extracting(GameEventFact.VisualCue::cueType)
                .containsExactly(VisualCueType.DAMAGE_TO_PLAYER);
    }

    @Test
    @DisplayName("Counterspell emits a countered cue sourced by Counterspell and naming the countered spell")
    void counteredCue() {
        LightningBolt bolt = new LightningBolt();
        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, bolt.getId());
        batches.clear();

        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(cues()).extracting(GameEventFact.VisualCue::cueType)
                .containsExactly(VisualCueType.SPELL_RESOLVED, VisualCueType.SPELL_COUNTERED);
        assertThat(cues().getLast()).satisfies(cue -> {
            assertThat(cue.sourceCardId()).isEqualTo(counterspell.getId());
            assertThat(cue.sourceName()).isEqualTo("Counterspell");
            assertThat(cue.targetCardId()).isEqualTo(bolt.getId());
        });
    }

    @Test
    @DisplayName("Mana Leak's unpaid tax emits a countered cue sourced by Mana Leak once the player declines")
    void counteredCueAfterDecliningToPay() {
        GrizzlyBears bears = new GrizzlyBears();
        ManaLeak leak = new ManaLeak();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.setHand(player2, List.of(leak));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        assertThat(cues()).noneMatch(cue -> cue.cueType() == VisualCueType.SPELL_COUNTERED);
        batches.clear();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(cues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.SPELL_COUNTERED);
            assertThat(cue.sourceCardId()).isEqualTo(leak.getId());
            assertThat(cue.targetCardId()).isEqualTo(bears.getId());
        });
    }

    @Test
    @DisplayName("Healing Salve emits a life-gained cue with the amount gained")
    void lifeGainedCue() {
        HealingSalve salve = new HealingSalve();
        harness.setHand(player1, List.of(salve));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, 0, player1.getId());
        batches.clear();

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(cues()).filteredOn(cue -> cue.cueType() == VisualCueType.LIFE_GAINED)
                .singleElement().satisfies(cue -> {
                    assertThat(cue.sourceCardId()).isEqualTo(salve.getId());
                    assertThat(cue.targetPlayerId()).isEqualTo(player1.getId());
                    assertThat(cue.amount()).isEqualTo(3);
                });
    }

    @Test
    @DisplayName("Sign in Blood emits a life-lost cue; damage to a player emits none")
    void lifeLostCue() {
        SignInBlood sign = new SignInBlood();
        harness.setHand(player1, List.of(sign));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        batches.clear();

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(cues()).filteredOn(cue -> cue.cueType() == VisualCueType.LIFE_LOST)
                .singleElement().satisfies(cue -> {
                    assertThat(cue.sourceCardId()).isEqualTo(sign.getId());
                    assertThat(cue.targetPlayerId()).isEqualTo(player2.getId());
                    assertThat(cue.amount()).isEqualTo(2);
                });

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        batches.clear();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(cues()).noneMatch(cue -> cue.cueType() == VisualCueType.LIFE_LOST);
    }

    @Test
    @DisplayName("Two +1/+1 counters from one spell emit one counters cue with amount 2 and the counter type")
    void countersAddedCueIsMerged() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        DragonscaleBoon boon = new DragonscaleBoon();
        harness.setHand(player1, List.of(boon));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, bears.getId());
        batches.clear();
        harness.getConn1().clearMessages();

        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(cues()).extracting(GameEventFact.VisualCue::cueType)
                .containsExactly(VisualCueType.SPELL_RESOLVED, VisualCueType.COUNTERS_ADDED);
        assertThat(cues().getLast()).satisfies(cue -> {
            assertThat(cue.sourceCardId()).isEqualTo(boon.getId());
            assertThat(cue.targetPermanentId()).isEqualTo(bears.getId());
            assertThat(cue.amount()).isEqualTo(2);
            assertThat(cue.counterType()).isEqualTo(CounterType.PLUS_ONE_PLUS_ONE);
        });
        assertThat(projectedCues()).anySatisfy(cue -> {
            assertThat(cue.get("type").asString()).isEqualTo("COUNTERS_ADDED");
            assertThat(cue.get("counterType").asString()).isEqualTo("PLUS_ONE_PLUS_ONE");
        });
    }

    @Test
    @DisplayName("Counters put on a permanent outside an action (test setup) are never reported")
    void countersFromSetupAreNotReported() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(cues()).noneMatch(cue -> cue.cueType() == VisualCueType.COUNTERS_ADDED);
    }

    private List<GameEventFact.VisualCue> cues() {
        return batches.stream()
                .flatMap(batch -> batch.events().stream())
                .map(envelope -> envelope.fact())
                .filter(GameEventFact.VisualCue.class::isInstance)
                .map(GameEventFact.VisualCue.class::cast)
                .toList();
    }

    private List<JsonNode> projectedCues() {
        List<JsonNode> cues = new ArrayList<>();
        harness.getConn1().getSentMessages().stream()
                .map(JSON::readTree)
                .filter(message -> "GAME_STATE".equals(message.path("type").asString()))
                .forEach(state -> state.get("visualCues").forEach(cues::add));
        return cues;
    }
}
