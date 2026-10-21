package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GravebaneZombie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Leave-the-battlefield cues ({@link VisualCueType#DESTROYED}, {@link VisualCueType#SACRIFICED},
 * {@link VisualCueType#EXILED}, {@link VisualCueType#RETURNED_TO_HAND}, {@link VisualCueType#DIED})
 * emitted by {@code PermanentRemovalService} once a permanent has actually left.
 */
@CardUsed({DoomBlade.class, GrizzlyBears.class, LightningBolt.class, Unsummon.class, SwordsToPlowshares.class,
        DiabolicEdict.class, DarksteelMyr.class, RoyalAssassin.class, KalitasTraitorOfGhet.class,
        GravebaneZombie.class})
class LeaveBattlefieldVisualCueTest extends BaseCardTest {

    private static final ObjectMapper JSON = new JacksonConfig().objectMapper();
    private static final Set<VisualCueType> LEAVE_TYPES = EnumSet.of(VisualCueType.DESTROYED,
            VisualCueType.SACRIFICED, VisualCueType.EXILED, VisualCueType.RETURNED_TO_HAND, VisualCueType.DIED);

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
    @DisplayName("Doom Blade emits DESTROYED with Doom Blade as the source, and it reaches the client")
    void doomBladeDestroys() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        DoomBlade doomBlade = new DoomBlade();
        harness.setHand(player1, List.of(doomBlade));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.getConn2().clearMessages();

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(leaveCues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.DESTROYED);
            assertThat(cue.sourceCardId()).isEqualTo(doomBlade.getId());
            assertThat(cue.sourceName()).isEqualTo("Doom Blade");
            assertThat(cue.sourceColors()).containsExactly(CardColor.BLACK);
            assertThat(cue.targetPermanentId()).isEqualTo(bears.getId());
            assertThat(cue.targetPlayerId()).isNull();
            assertThat(cue.amount()).isNull();
        });
        assertThat(projectedCues()).anySatisfy(cue -> {
            assertThat(cue.get("type").asString()).isEqualTo("DESTROYED");
            assertThat(cue.get("targetPermanentId").asString()).isEqualTo(bears.getId().toString());
        });
    }

    @Test
    @DisplayName("A creature destroyed by an activated ability has the ability's permanent as the source")
    void abilityDestroys() {
        Permanent assassin = addCreatureReady(player1, new RoyalAssassin());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(leaveCues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.DESTROYED);
            assertThat(cue.sourceCardId()).isEqualTo(assassin.getCard().getId());
            assertThat(cue.sourceName()).isEqualTo("Royal Assassin");
            assertThat(cue.targetPermanentId()).isEqualTo(bears.getId());
        });
    }

    @Test
    @DisplayName("Lethal damage emits DIED with no source, after the spell and damage cues")
    void lethalDamageDies() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(allCues()).extracting(GameEventFact.VisualCue::cueType)
                .containsExactly(VisualCueType.SPELL_CAST, VisualCueType.SPELL_RESOLVED,
                        VisualCueType.DAMAGE_TO_PERMANENT, VisualCueType.DIED);
        assertThat(leaveCues()).singleElement().satisfies(cue -> {
            assertThat(cue.targetPermanentId()).isEqualTo(bears.getId());
            assertThat(cue.sourceCardId()).isNull();
            assertThat(cue.sourceName()).isNull();
            assertThat(cue.sourceColors()).isEmpty();
        });
    }

    @Test
    @DisplayName("Unsummon emits RETURNED_TO_HAND with Unsummon as the source")
    void unsummonReturnsToHand() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Unsummon unsummon = new Unsummon();
        harness.setHand(player1, List.of(unsummon));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(leaveCues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.RETURNED_TO_HAND);
            assertThat(cue.sourceCardId()).isEqualTo(unsummon.getId());
            assertThat(cue.targetPermanentId()).isEqualTo(bears.getId());
        });
    }

    @Test
    @DisplayName("Swords to Plowshares emits EXILED with Swords as the source")
    void swordsExiles() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        SwordsToPlowshares swords = new SwordsToPlowshares();
        harness.setHand(player1, List.of(swords));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(leaveCues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.EXILED);
            assertThat(cue.sourceCardId()).isEqualTo(swords.getId());
            assertThat(cue.targetPermanentId()).isEqualTo(bears.getId());
        });
    }

    @Test
    @DisplayName("Diabolic Edict emits SACRIFICED with the Edict as the source")
    void edictSacrifices() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        DiabolicEdict edict = new DiabolicEdict();
        harness.setHand(player1, List.of(edict));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(leaveCues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.SACRIFICED);
            assertThat(cue.sourceCardId()).isEqualTo(edict.getId());
            assertThat(cue.targetPermanentId()).isEqualTo(bears.getId());
        });
    }

    @Test
    @DisplayName("An indestructible creature hit by Doom Blade emits no destroy cue")
    void indestructibleEmitsNothing() {
        Permanent myr = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, myr.getId());

        harness.assertOnBattlefield(player2, "Darksteel Myr");
        assertThat(leaveCues()).isEmpty();
    }

    @Test
    @DisplayName("A regenerated creature emits no destroy cue")
    void regeneratedEmitsNothing() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setRegenerationShield(1);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(leaveCues()).isEmpty();
    }

    @Test
    @DisplayName("A face-down creature's destroy cue carries no name of it, on the engine fact or the wire")
    void faceDownCreatureStaysAnonymous() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.getConn1().clearMessages();

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(leaveCues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.DESTROYED);
            assertThat(cue.targetPermanentId()).isEqualTo(bears.getId());
            assertThat(cue.sourceName()).isEqualTo("Doom Blade");
        });
        List<JsonNode> projected = projectedCues(harness.getConn1().getSentMessages());
        assertThat(projected).isNotEmpty();
        assertThat(projected).allSatisfy(cue -> assertThat(cue.toString()).doesNotContain("Grizzly Bears"));
    }

    @Test
    @DisplayName("No cue while a death replacement choice is pending; the cue follows the actual move")
    void pendingReplacementChoiceWaitsForTheMove() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent gravebane = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(leaveCues()).isEmpty();

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        assertThat(gd.findExiledCard(gravebane.getCard().getId())).isNotNull();
        assertThat(leaveCues()).singleElement().satisfies(cue -> {
            assertThat(cue.cueType()).isEqualTo(VisualCueType.SACRIFICED);
            assertThat(cue.targetPermanentId()).isEqualTo(gravebane.getId());
        });
    }

    private List<GameEventFact.VisualCue> allCues() {
        return batches.stream()
                .flatMap(batch -> batch.events().stream())
                .map(envelope -> envelope.fact())
                .filter(GameEventFact.VisualCue.class::isInstance)
                .map(GameEventFact.VisualCue.class::cast)
                .toList();
    }

    private List<GameEventFact.VisualCue> leaveCues() {
        return allCues().stream().filter(cue -> LEAVE_TYPES.contains(cue.cueType())).toList();
    }

    private List<JsonNode> projectedCues() {
        return projectedCues(harness.getConn2().getSentMessages());
    }

    private static List<JsonNode> projectedCues(List<String> messages) {
        List<JsonNode> cues = new ArrayList<>();
        messages.stream()
                .map(JSON::readTree)
                .filter(message -> "GAME_STATE".equals(message.path("type").asString()))
                .forEach(state -> state.get("visualCues").forEach(cues::add));
        return cues;
    }
}
