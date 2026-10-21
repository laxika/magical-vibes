package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.cards.e.EarthElemental;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Pariah;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.event.VisualCueType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Damage cues ({@link VisualCueType#DAMAGE_TO_PERMANENT}, {@link VisualCueType#DAMAGE_TO_PLAYER})
 * emitted by the noncombat and combat damage paths, with the amount actually dealt.
 */
@CardUsed({LightningBolt.class, GrizzlyBears.class, EarthElemental.class, HealingSalve.class, Shock.class,
        LavaAxe.class, Fog.class, Pariah.class})
class DamageVisualCueTest extends BaseCardTest {

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
    @DisplayName("Lightning Bolt on a creature emits one permanent-damage cue with Bolt as the source")
    void lightningBoltOnACreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new EarthElemental());
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.getConn1().clearMessages();

        harness.castAndResolveInstant(player1, 0, elemental.getId());

        assertThat(damageCues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.DAMAGE_TO_PERMANENT);
            assertThat(cue.sourceCardId()).isEqualTo(bolt.getId());
            assertThat(cue.sourceName()).isEqualTo("Lightning Bolt");
            assertThat(cue.sourceColors()).containsExactly(CardColor.RED);
            assertThat(cue.targetPermanentId()).isEqualTo(elemental.getId());
            assertThat(cue.targetPlayerId()).isNull();
            assertThat(cue.amount()).isEqualTo(3);
        });
        assertThat(projectedCueTypes()).contains("DAMAGE_TO_PERMANENT");
    }

    @Test
    @DisplayName("Lightning Bolt on a player emits one player-damage cue")
    void lightningBoltOnAPlayer() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(damageCues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.DAMAGE_TO_PLAYER);
            assertThat(cue.sourceCardId()).isEqualTo(bolt.getId());
            assertThat(cue.targetPlayerId()).isEqualTo(player2.getId());
            assertThat(cue.targetPermanentId()).isNull();
            assertThat(cue.amount()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("A combat trade emits one cue per damage assignment, sourced by each creature")
    void combatTrade() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, attacker)));
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, blocker), indexOf(player1, attacker))));
        resolveCombat(player1);

        assertThat(damageCues())
                .extracting(GameEventFact.VisualCue::cueType, GameEventFact.VisualCue::sourceCardId,
                        GameEventFact.VisualCue::targetPermanentId, GameEventFact.VisualCue::amount)
                .containsExactlyInAnyOrder(
                        tuple(VisualCueType.DAMAGE_TO_PERMANENT,
                                attacker.getCard().getId(), blocker.getId(), 2),
                        tuple(VisualCueType.DAMAGE_TO_PERMANENT,
                                blocker.getCard().getId(), attacker.getId(), 2));
    }

    @Test
    @DisplayName("Unblocked combat damage emits a player-damage cue from the attacker")
    void unblockedCombatDamageToAPlayer() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(indexOf(player1, attacker)));
        resolveCombat(player1);

        assertThat(damageCues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.DAMAGE_TO_PLAYER);
            assertThat(cue.sourceCardId()).isEqualTo(attacker.getCard().getId());
            assertThat(cue.sourceName()).isEqualTo("Grizzly Bears");
            assertThat(cue.targetPlayerId()).isEqualTo(player2.getId());
            assertThat(cue.amount()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("A face-down attacker's damage cue keeps its card id but hides its name and colors")
    void faceDownAttackerIsAnonymous() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        declareAttackers(List.of(indexOf(player1, attacker)));
        resolveCombat(player1);

        assertThat(damageCues()).singleElement().satisfies(cue -> {
            assertThat(cue.sourceCardId()).isEqualTo(attacker.getCard().getId());
            assertThat(cue.sourceName()).isNull();
            assertThat(cue.sourceColors()).isEmpty();
        });
    }

    @Test
    @DisplayName("Fully prevented noncombat damage emits no cue")
    void fullyPreventedNoncombatDamage() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HealingSalve()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, 1, bears.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        batches.clear();

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(damageCues()).isEmpty();
    }

    @Test
    @DisplayName("Partly prevented damage emits a cue with only the damage dealt")
    void partlyPreventedDamageCarriesTheDealtAmount() {
        harness.setHand(player1, List.of(new HealingSalve()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);
        batches.clear();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(damageCues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.DAMAGE_TO_PLAYER);
            assertThat(cue.amount()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Fully prevented combat damage emits no cue")
    void fullyPreventedCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        declareAttackers(List.of(indexOf(player1, attacker)));
        harness.castAndResolveInstant(player2, 0);
        batches.clear();
        resolveCombat(player1);

        harness.assertLife(player2, 20);
        assertThat(damageCues()).isEmpty();
    }

    @Test
    @DisplayName("Noncombat damage a replacement redirects emits a cue for the new recipient only")
    void redirectedNoncombatDamage() {
        Permanent elemental = addCreatureReady(player2, new EarthElemental());
        Permanent pariah = harness.addToBattlefieldAndReturn(player2, new Pariah());
        pariah.setAttachedTo(elemental.getId());
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
        assertThat(damageCues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.DAMAGE_TO_PERMANENT);
            assertThat(cue.sourceCardId()).isEqualTo(bolt.getId());
            assertThat(cue.targetPermanentId()).isEqualTo(elemental.getId());
            assertThat(cue.amount()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("Combat damage a replacement redirects emits a cue for the new recipient only")
    void redirectedCombatDamage() {
        Permanent elemental = addCreatureReady(player2, new EarthElemental());
        Permanent pariah = harness.addToBattlefieldAndReturn(player2, new Pariah());
        pariah.setAttachedTo(elemental.getId());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(indexOf(player1, attacker)));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat(player1);

        harness.assertLife(player2, 20);
        assertThat(damageCues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.DAMAGE_TO_PERMANENT);
            assertThat(cue.sourceCardId()).isEqualTo(attacker.getCard().getId());
            assertThat(cue.targetPermanentId()).isEqualTo(elemental.getId());
            assertThat(cue.amount()).isEqualTo(2);
        });
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private List<GameEventFact.VisualCue> damageCues() {
        return batches.stream()
                .flatMap(batch -> batch.events().stream())
                .map(envelope -> envelope.fact())
                .filter(GameEventFact.VisualCue.class::isInstance)
                .map(GameEventFact.VisualCue.class::cast)
                .filter(cue -> cue.cueType() == VisualCueType.DAMAGE_TO_PERMANENT
                        || cue.cueType() == VisualCueType.DAMAGE_TO_PLAYER)
                .toList();
    }

    private List<String> projectedCueTypes() {
        List<String> types = new ArrayList<>();
        harness.getConn1().getSentMessages().stream()
                .map(JSON::readTree)
                .filter(message -> "GAME_STATE".equals(message.path("type").asString()))
                .forEach(state -> state.get("visualCues").forEach(cue -> types.add(cue.get("type").asString())));
        return types;
    }
}
