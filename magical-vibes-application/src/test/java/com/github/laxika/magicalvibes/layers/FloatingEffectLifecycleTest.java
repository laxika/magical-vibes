package com.github.laxika.magicalvibes.layers;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lifecycle of {@link FloatingContinuousEffect}s on {@link GameData} (CR 613 layer-system
 * migration, step 2): timestamp stamping on insertion, duration-based expiry (cleanup step,
 * source leaving the battlefield, source becoming unattached, controller's next turn), and
 * AI simulation-copy semantics. Nothing consumes these effects yet — these tests pin down
 * the store's bookkeeping only.
 */
class FloatingEffectLifecycleTest extends BaseCardTest {

    @Nested
    @DisplayName("Timestamp stamping")
    class Stamping {

        @Test
        @DisplayName("addFloatingEffect stamps the next CR 613.7 timestamp on insertion")
        void addStampsTimestamp() {
            FloatingContinuousEffect first = gd.addFloatingEffect(
                    floating(null, player1.getId(), EffectDuration.UNTIL_END_OF_TURN));
            FloatingContinuousEffect second = gd.addFloatingEffect(
                    floating(null, player1.getId(), EffectDuration.UNTIL_END_OF_TURN));

            assertThat(first.timestamp()).isPositive();
            assertThat(second.timestamp()).isGreaterThan(first.timestamp());
            assertThat(second.timestamp()).isEqualTo(gd.timestampCounter);
            assertThat(gd.floatingEffects).containsExactly(first, second);
        }
    }

    @Nested
    @DisplayName("Until end of turn")
    class EndOfTurnExpiry {

        @Test
        @DisplayName("An UNTIL_END_OF_TURN effect disappears at the cleanup step; other durations survive")
        void endOfTurnEffectExpiresAtCleanup() {
            gd.addFloatingEffect(floating(null, player1.getId(), EffectDuration.UNTIL_END_OF_TURN));
            FloatingContinuousEffect survivor = gd.addFloatingEffect(
                    floating(null, player1.getId(), EffectDuration.UNTIL_YOUR_NEXT_TURN));

            GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

            assertThat(gd.floatingEffects).containsExactly(survivor);
        }
    }

    @Nested
    @DisplayName("While source on battlefield")
    class SourceLeavesExpiry {

        @Test
        @DisplayName("A WHILE_SOURCE_ON_BATTLEFIELD effect disappears when its source is destroyed")
        void effectExpiresWhenSourceDestroyed() {
            Permanent source = addCreatureReady(player1, createCreature("Grizzly Bears"));
            Permanent other = addCreatureReady(player1, createCreature("Coral Merfolk"));
            gd.addFloatingEffect(floating(source.getId(), player1.getId(),
                    EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD));
            FloatingContinuousEffect unrelated = gd.addFloatingEffect(floating(other.getId(),
                    player1.getId(), EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD));

            harness.getPermanentRemovalService().tryDestroyPermanent(gd, source);

            harness.assertInGraveyard(player1, "Grizzly Bears");
            assertThat(gd.floatingEffects).containsExactly(unrelated);
        }
    }

    @Nested
    @DisplayName("While attached")
    class UnattachExpiry {

        @Test
        @DisplayName("A WHILE_ATTACHED effect disappears when the equipment becomes unattached")
        void effectExpiresWhenEquipmentUnattached() {
            Permanent creature = addCreatureReady(player1, createCreature("Grizzly Bears"));
            Permanent equipment = new Permanent(createEquipment("Loxodon Warhammer"));
            equipment.setAttachedTo(creature.getId());
            gd.playerBattlefields.get(player1.getId()).add(equipment);
            gd.addFloatingEffect(floating(equipment.getId(), player1.getId(),
                    EffectDuration.WHILE_ATTACHED));
            FloatingContinuousEffect sourceBacked = gd.addFloatingEffect(floating(equipment.getId(),
                    player1.getId(), EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD));

            // Destroying the equipped creature unattaches the equipment (CR 301.5c) but keeps
            // it on the battlefield, so only the WHILE_ATTACHED effect ends.
            harness.getPermanentRemovalService().tryDestroyPermanent(gd, creature);

            assertThat(equipment.getAttachedTo()).isNull();
            assertThat(gd.floatingEffects).containsExactly(sourceBacked);
        }
    }

    @Nested
    @DisplayName("Until controller's next turn")
    class TurnStartExpiry {

        @Test
        @DisplayName("expireFloatingEffectsAtTurnStart removes only the given player's UNTIL_YOUR_NEXT_TURN effects")
        void turnStartExpiryIsPerController() {
            gd.addFloatingEffect(floating(null, player1.getId(), EffectDuration.UNTIL_YOUR_NEXT_TURN));
            FloatingContinuousEffect opponents = gd.addFloatingEffect(
                    floating(null, player2.getId(), EffectDuration.UNTIL_YOUR_NEXT_TURN));
            FloatingContinuousEffect permanent = gd.addFloatingEffect(
                    floating(null, player1.getId(), EffectDuration.PERMANENT));

            List<FloatingContinuousEffect> removed = gd.expireFloatingEffectsAtTurnStart(player1.getId());

            assertThat(removed).hasSize(1);
            assertThat(gd.floatingEffects).containsExactly(opponents, permanent);
        }
    }

    @Nested
    @DisplayName("AI simulation copy")
    class SimulationCopy {

        @Test
        @DisplayName("simulationCopy carries floating effects and the copy's list is independent")
        void copyCarriesEffectsIndependently() {
            FloatingContinuousEffect effect = gd.addFloatingEffect(
                    floating(null, player1.getId(), EffectDuration.UNTIL_END_OF_TURN));

            GameData copy = gd.simulationCopy();

            assertThat(copy.floatingEffects).containsExactly(effect);
            copy.floatingEffects.clear();
            copy.addFloatingEffect(floating(null, player2.getId(), EffectDuration.PERMANENT));
            assertThat(gd.floatingEffects).containsExactly(effect);
        }
    }

    // ===== helpers =====

    private FloatingContinuousEffect floating(UUID sourcePermanentId, UUID controllerId, EffectDuration duration) {
        return new FloatingContinuousEffect(UUID.randomUUID(), "Test Source", sourcePermanentId,
                controllerId, new BoostTargetCreatureEffect(3, 3), null, null, null, duration, 0L);
    }

    private Card createCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        return card;
    }

    private Card createEquipment(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        return card;
    }
}
