package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfThisPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGetsPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDamageSourcePermanentToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DamageTriggerCollectorServiceTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private PermanentRemovalService permanentRemovalService;

    @Mock
    private CreatureControlService creatureControlService;

    @InjectMocks
    private DamageTriggerCollectorService sut;

    private TriggerCollectorRegistry registry;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");

        registry = new TriggerCollectorRegistry();
        TriggerCollectorRegistry.scanBean(sut, registry);
    }

    // ===== Helpers =====

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private static Permanent createPermanent(String name) {
        return new Permanent(createCard(name));
    }

    private TriggerMatchContext match(Permanent perm, UUID controllerId,
            com.github.laxika.magicalvibes.model.effect.CardEffect effect) {
        return new TriggerMatchContext(gd, perm, controllerId, effect);
    }

    // ===== ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU — ReturnDamageSourcePermanentToHandEffect =====

    @Nested
    @DisplayName("ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU — ReturnDamageSourcePermanentToHandEffect")
    class BounceOnDamage {

        @Test
        @DisplayName("bounces the damage source to its owner's hand and returns true")
        void bouncesSourceAndReturnsTrue() {
            Permanent triggerPerm = createPermanent("Dissipation Field");
            Permanent sourcePerm = createPermanent("Grizzly Bears");
            var effect = new ReturnDamageSourcePermanentToHandEffect();
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), true);

            when(gameQueryService.findPermanentById(gd, sourcePerm.getId())).thenReturn(sourcePerm);
            when(permanentRemovalService.removePermanentToHand(gd, sourcePerm)).thenReturn(true);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isTrue();
            verify(permanentRemovalService).removePermanentToHand(gd, sourcePerm);
            verify(permanentRemovalService).removeOrphanedAuras(gd);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }

        @Test
        @DisplayName("returns false when source permanent is no longer on the battlefield")
        void returnsFalseWhenSourceGone() {
            Permanent triggerPerm = createPermanent("Dissipation Field");
            UUID missingSourceId = UUID.randomUUID();
            var effect = new ReturnDamageSourcePermanentToHandEffect();
            var ctx = new TriggerContext.DamageToController(player1Id, missingSourceId, true);

            when(gameQueryService.findPermanentById(gd, missingSourceId)).thenReturn(null);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isFalse();
            verify(permanentRemovalService, never()).removePermanentToHand(any(), any());
        }

        @Test
        @DisplayName("does not broadcast when removePermanentToHand returns false")
        void noBroadcastWhenRemoveFails() {
            Permanent triggerPerm = createPermanent("Dissipation Field");
            Permanent sourcePerm = createPermanent("Grizzly Bears");
            var effect = new ReturnDamageSourcePermanentToHandEffect();
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), true);

            when(gameQueryService.findPermanentById(gd, sourcePerm.getId())).thenReturn(sourcePerm);
            when(permanentRemovalService.removePermanentToHand(gd, sourcePerm)).thenReturn(false);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isTrue();
            verify(permanentRemovalService, never()).removeOrphanedAuras(any());
            verify(gameBroadcastService, never()).logAndBroadcast(any(), any());
        }
    }

    // ===== ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU — DamageSourceControllerGainsControlOfThisPermanentEffect =====

    @Nested
    @DisplayName("ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU — DamageSourceControllerGainsControlOfThisPermanentEffect")
    class ControlTheftOnDamage {

        @Test
        @DisplayName("steals the trigger permanent when combat damage from opponent creature")
        void stealsOnCombatDamage() {
            Permanent triggerPerm = createPermanent("Beguiler of Wills");
            Permanent sourcePerm = createPermanent("Grizzly Bears");
            var effect = new DamageSourceControllerGainsControlOfThisPermanentEffect(true, true);
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), true);

            when(gameQueryService.findPermanentById(gd, sourcePerm.getId())).thenReturn(sourcePerm);
            when(gameQueryService.isCreature(gd, sourcePerm)).thenReturn(true);
            when(gameQueryService.findPermanentController(gd, sourcePerm.getId())).thenReturn(player2Id);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isTrue();
            verify(creatureControlService).stealPermanent(gd, player2Id, triggerPerm);
            assertThat(gd.permanentControlStolenCreatures).contains(triggerPerm.getId());
        }

        @Test
        @DisplayName("returns false when combatOnly=true but damage is noncombat")
        void returnsFalseForNoncombatWhenCombatOnly() {
            Permanent triggerPerm = createPermanent("Beguiler of Wills");
            Permanent sourcePerm = createPermanent("Grizzly Bears");
            var effect = new DamageSourceControllerGainsControlOfThisPermanentEffect(true, false);
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), false);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isFalse();
            verify(creatureControlService, never()).stealPermanent(any(), any(), any());
        }

        @Test
        @DisplayName("returns false when source permanent is gone")
        void returnsFalseWhenSourceGone() {
            Permanent triggerPerm = createPermanent("Beguiler of Wills");
            UUID missingSourceId = UUID.randomUUID();
            var effect = new DamageSourceControllerGainsControlOfThisPermanentEffect(false, false);
            var ctx = new TriggerContext.DamageToController(player1Id, missingSourceId, true);

            when(gameQueryService.findPermanentById(gd, missingSourceId)).thenReturn(null);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when creatureOnly=true but source is not a creature")
        void returnsFalseWhenSourceNotCreature() {
            Permanent triggerPerm = createPermanent("Beguiler of Wills");
            Permanent sourcePerm = createPermanent("Some Artifact");
            var effect = new DamageSourceControllerGainsControlOfThisPermanentEffect(false, true);
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), true);

            when(gameQueryService.findPermanentById(gd, sourcePerm.getId())).thenReturn(sourcePerm);
            when(gameQueryService.isCreature(gd, sourcePerm)).thenReturn(false);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when source controller is the same as damaged player")
        void returnsFalseWhenSourceControllerIsDamagedPlayer() {
            Permanent triggerPerm = createPermanent("Beguiler of Wills");
            Permanent sourcePerm = createPermanent("Grizzly Bears");
            var effect = new DamageSourceControllerGainsControlOfThisPermanentEffect(false, false);
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), true);

            when(gameQueryService.findPermanentById(gd, sourcePerm.getId())).thenReturn(sourcePerm);
            when(gameQueryService.findPermanentController(gd, sourcePerm.getId())).thenReturn(player1Id);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when source controller is null")
        void returnsFalseWhenSourceControllerNull() {
            Permanent triggerPerm = createPermanent("Beguiler of Wills");
            Permanent sourcePerm = createPermanent("Grizzly Bears");
            var effect = new DamageSourceControllerGainsControlOfThisPermanentEffect(false, false);
            var ctx = new TriggerContext.DamageToController(player1Id, sourcePerm.getId(), true);

            when(gameQueryService.findPermanentById(gd, sourcePerm.getId())).thenReturn(sourcePerm);
            when(gameQueryService.findPermanentController(gd, sourcePerm.getId())).thenReturn(null);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, effect, ctx);

            assertThat(result).isFalse();
        }
    }

    // ===== ON_DEALT_DAMAGE — DamageSourceControllerSacrificesPermanentsEffect =====

    @Nested
    @DisplayName("ON_DEALT_DAMAGE — DamageSourceControllerSacrificesPermanentsEffect")
    class DamageSourceSacrifice {

        @Test
        @DisplayName("adds triggered ability to stack with damage count and source controller")
        void addsTriggeredAbilityWithDamageCount() {
            Permanent damagedCreature = createPermanent("Phyrexian Obliterator");
            var effect = new DamageSourceControllerSacrificesPermanentsEffect(0, null);
            UUID damageSourceControllerId = player2Id;
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 3, damageSourceControllerId);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            boolean result = registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getFirst();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getEffectsToResolve()).hasSize(1);
            assertThat(stackEntry.getEffectsToResolve().getFirst())
                    .isInstanceOf(DamageSourceControllerSacrificesPermanentsEffect.class);
            var resolvedEffect = (DamageSourceControllerSacrificesPermanentsEffect) stackEntry.getEffectsToResolve().getFirst();
            assertThat(resolvedEffect.count()).isEqualTo(3);
            assertThat(resolvedEffect.sacrificingPlayerId()).isEqualTo(damageSourceControllerId);
        }

        @Test
        @DisplayName("uses original effect when damageSourceControllerId is null")
        void usesOriginalEffectWhenControllerNull() {
            Permanent damagedCreature = createPermanent("Phyrexian Obliterator");
            var effect = new DamageSourceControllerSacrificesPermanentsEffect(5, player2Id);
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 0, null);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(gd.stack).hasSize(1);
            var resolvedEffect = (DamageSourceControllerSacrificesPermanentsEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
            assertThat(resolvedEffect.count()).isEqualTo(5);
            assertThat(resolvedEffect.sacrificingPlayerId()).isEqualTo(player2Id);
        }
    }

    // ===== ON_DEALT_DAMAGE — DamageSourceControllerGetsPoisonCounterEffect =====

    @Nested
    @DisplayName("ON_DEALT_DAMAGE — DamageSourceControllerGetsPoisonCounterEffect")
    class DamageSourcePoisonCounter {

        @Test
        @DisplayName("adds triggered ability with damage source controller")
        void addsTriggeredAbilityWithSourceController() {
            Permanent damagedCreature = createPermanent("Poisonous Creature");
            var effect = new DamageSourceControllerGetsPoisonCounterEffect(null);
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 2, player2Id);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            boolean result = registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var resolvedEffect = (DamageSourceControllerGetsPoisonCounterEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
            assertThat(resolvedEffect.damageSourceControllerId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("uses original effect when damageSourceControllerId is null")
        void usesOriginalEffectWhenControllerNull() {
            Permanent damagedCreature = createPermanent("Poisonous Creature");
            var effect = new DamageSourceControllerGetsPoisonCounterEffect(player1Id);
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 2, null);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(gd.stack).hasSize(1);
            var resolvedEffect = (DamageSourceControllerGetsPoisonCounterEffect) gd.stack.getFirst().getEffectsToResolve().getFirst();
            assertThat(resolvedEffect.damageSourceControllerId()).isEqualTo(player1Id);
        }
    }

    // ===== ON_DEALT_DAMAGE — default handler =====

    @Nested
    @DisplayName("ON_DEALT_DAMAGE — default handler")
    class DealtDamageDefault {

        @Test
        @DisplayName("does not add to stack when controller is null")
        void doesNotAddWhenControllerNull() {
            Permanent damagedCreature = createPermanent("Grizzly Bears");
            var effect = new ReturnDamageSourcePermanentToHandEffect();
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 2, player2Id);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(null);

            registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("adds triggered ability for unrecognized effect via default handler")
        void addsTriggeredAbilityForUnrecognizedEffect() {
            Permanent damagedCreature = createPermanent("Custom Creature");
            var effect = new ReturnDamageSourcePermanentToHandEffect();
            var ctx = new TriggerContext.DamageToCreature(damagedCreature, 2, player2Id);

            when(gameQueryService.findPermanentController(gd, damagedCreature.getId())).thenReturn(player1Id);

            boolean result = registry.dispatch(
                    match(damagedCreature, player1Id, effect),
                    EffectSlot.ON_DEALT_DAMAGE, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var stackEntry = gd.stack.getFirst();
            assertThat(stackEntry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(stackEntry.getControllerId()).isEqualTo(player1Id);
            assertThat(stackEntry.getEffectsToResolve()).containsExactly(effect);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }
    }
}
