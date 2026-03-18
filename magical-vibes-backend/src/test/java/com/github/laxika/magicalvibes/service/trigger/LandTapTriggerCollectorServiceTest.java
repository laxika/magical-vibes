package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AddExtraManaOfChosenColorOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentTappedLandDoesntUntapEffect;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LandTapTriggerCollectorServiceTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private DamagePreventionService damagePreventionService;

    @Mock
    private PermanentRemovalService permanentRemovalService;

    @InjectMocks
    private LandTapTriggerCollectorService sut;

    private TriggerCollectorRegistry registry;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerManaPools.put(player1Id, new ManaPool());
        gd.playerManaPools.put(player2Id, new ManaPool());

        registry = new TriggerCollectorRegistry();
        TriggerCollectorRegistry.scanBean(sut, registry);
    }

    // ===== Helpers =====

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        return card;
    }

    private static Card createLandCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        return card;
    }

    private static Card createLandCardWithMana(String name, ManaColor color) {
        Card card = createLandCard(name);
        card.addEffect(EffectSlot.ON_TAP, new AwardManaEffect(color));
        return card;
    }

    private static Permanent createPermanent(String name) {
        return new Permanent(createCard(name));
    }

    private static Permanent createLandPermanent(String name, ManaColor color) {
        return new Permanent(createLandCardWithMana(name, color));
    }

    private TriggerMatchContext match(Permanent perm, UUID controllerId, CardEffect effect) {
        return new TriggerMatchContext(gd, perm, controllerId, effect);
    }

    // ===== ON_ANY_PLAYER_TAPS_LAND — DealDamageOnLandTapEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_TAPS_LAND — DealDamageOnLandTapEffect")
    class DealDamageOnLandTap {

        @Test
        @DisplayName("deals damage to the tapping player and returns true")
        void dealsDamageToTappingPlayer() {
            Permanent manabarbs = createPermanent("Manabarbs");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.LandTap(player2Id, tappedLandId);

            int lifeBefore = gd.getLife(player2Id);

            when(gameQueryService.applyDamageMultiplier(gd, 1)).thenReturn(1);
            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(1))).thenReturn(1);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(1), any()))
                    .thenReturn(1);
            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);

            boolean result = registry.dispatch(
                    match(manabarbs, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore - 1);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }

        @Test
        @DisplayName("also damages the controller when they tap a land")
        void damagesControllerToo() {
            Permanent manabarbs = createPermanent("Manabarbs");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.LandTap(player1Id, tappedLandId);

            int lifeBefore = gd.getLife(player1Id);

            when(gameQueryService.applyDamageMultiplier(gd, 1)).thenReturn(1);
            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player1Id), eq(1))).thenReturn(1);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player1Id), eq(1), any()))
                    .thenReturn(1);
            when(gameQueryService.canPlayerLifeChange(gd, player1Id)).thenReturn(true);

            boolean result = registry.dispatch(
                    match(manabarbs, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player1Id)).isEqualTo(lifeBefore - 1);
        }

        @Test
        @DisplayName("does not deal damage when source damage is prevented globally")
        void noDamageWhenSourcePrevented() {
            Permanent manabarbs = createPermanent("Manabarbs");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.LandTap(player2Id, tappedLandId);

            int lifeBefore = gd.getLife(player2Id);

            when(gameQueryService.applyDamageMultiplier(gd, 1)).thenReturn(1);
            when(gameQueryService.isDamageFromSourcePrevented(eq(gd), any())).thenReturn(true);

            boolean result = registry.dispatch(
                    match(manabarbs, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("does not deal damage when source damage is prevented for player")
        void noDamageWhenSourcePreventedForPlayer() {
            Permanent manabarbs = createPermanent("Manabarbs");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.LandTap(player2Id, tappedLandId);

            int lifeBefore = gd.getLife(player2Id);

            when(gameQueryService.applyDamageMultiplier(gd, 1)).thenReturn(1);
            when(damagePreventionService.isSourceDamagePreventedForPlayer(gd, player2Id, manabarbs.getId()))
                    .thenReturn(true);

            boolean result = registry.dispatch(
                    match(manabarbs, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("does not deal damage when color damage prevention applies")
        void noDamageWhenColorPreventionApplies() {
            Permanent manabarbs = createPermanent("Manabarbs");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.LandTap(player2Id, tappedLandId);

            int lifeBefore = gd.getLife(player2Id);

            when(gameQueryService.applyDamageMultiplier(gd, 1)).thenReturn(1);
            when(damagePreventionService.applyColorDamagePreventionForPlayer(eq(gd), eq(player2Id), any()))
                    .thenReturn(true);

            boolean result = registry.dispatch(
                    match(manabarbs, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("does not deal damage when permanent is prevented from dealing damage")
        void noDamageWhenPermanentPrevented() {
            Permanent manabarbs = createPermanent("Manabarbs");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.LandTap(player2Id, tappedLandId);

            gd.permanentsPreventedFromDealingDamage.add(manabarbs.getId());
            int lifeBefore = gd.getLife(player2Id);

            when(gameQueryService.applyDamageMultiplier(gd, 1)).thenReturn(1);

            boolean result = registry.dispatch(
                    match(manabarbs, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("applies damage multiplier")
        void appliesDamageMultiplier() {
            Permanent manabarbs = createPermanent("Manabarbs");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.LandTap(player2Id, tappedLandId);

            int lifeBefore = gd.getLife(player2Id);

            when(gameQueryService.applyDamageMultiplier(gd, 1)).thenReturn(2);
            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(2))).thenReturn(2);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(2), any()))
                    .thenReturn(2);
            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);

            registry.dispatch(
                    match(manabarbs, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore - 2);
        }

        @Test
        @DisplayName("tracks player as dealt damage this turn")
        void tracksPlayerDealtDamageThisTurn() {
            Permanent manabarbs = createPermanent("Manabarbs");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.LandTap(player2Id, tappedLandId);

            when(gameQueryService.applyDamageMultiplier(gd, 1)).thenReturn(1);
            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(1))).thenReturn(1);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(1), any()))
                    .thenReturn(1);

            registry.dispatch(
                    match(manabarbs, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(gd.playersDealtDamageThisTurn).contains(player2Id);
        }

        @Test
        @DisplayName("gives poison counters when damage should be dealt as infect")
        void givesPoisonCountersWithInfect() {
            Permanent manabarbs = createPermanent("Manabarbs");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.LandTap(player2Id, tappedLandId);

            int lifeBefore = gd.getLife(player2Id);

            when(gameQueryService.applyDamageMultiplier(gd, 1)).thenReturn(1);
            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(1))).thenReturn(1);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(1), any()))
                    .thenReturn(1);
            when(gameQueryService.shouldDamageBeDealtAsInfect(gd, player2Id)).thenReturn(true);
            when(gameQueryService.canPlayerGetPoisonCounters(gd, player2Id)).thenReturn(true);

            registry.dispatch(
                    match(manabarbs, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(gd.playerPoisonCounters.getOrDefault(player2Id, 0)).isEqualTo(1);
            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("does not give poison counters when player can't get them")
        void noPoisonWhenPlayerCantGetThem() {
            Permanent manabarbs = createPermanent("Manabarbs");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.LandTap(player2Id, tappedLandId);

            when(gameQueryService.applyDamageMultiplier(gd, 1)).thenReturn(1);
            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(1))).thenReturn(1);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(1), any()))
                    .thenReturn(1);
            when(gameQueryService.shouldDamageBeDealtAsInfect(gd, player2Id)).thenReturn(true);
            // canPlayerGetPoisonCounters defaults to false

            registry.dispatch(
                    match(manabarbs, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(gd.playerPoisonCounters).doesNotContainKey(player2Id);
        }

        @Test
        @DisplayName("does not track damage when prevention shield reduces damage to zero")
        void noDamageTrackingWhenShieldReducesToZero() {
            Permanent manabarbs = createPermanent("Manabarbs");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.LandTap(player2Id, tappedLandId);

            int lifeBefore = gd.getLife(player2Id);

            when(gameQueryService.applyDamageMultiplier(gd, 1)).thenReturn(1);
            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(1))).thenReturn(0);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(0), any()))
                    .thenReturn(0);

            registry.dispatch(
                    match(manabarbs, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
            assertThat(gd.playersDealtDamageThisTurn).doesNotContain(player2Id);
        }

        @Test
        @DisplayName("does not change life when player life can't change")
        void noLifeChangeWhenPrevented() {
            Permanent manabarbs = createPermanent("Manabarbs");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new DealDamageOnLandTapEffect(1);
            var ctx = new TriggerContext.LandTap(player2Id, tappedLandId);

            int lifeBefore = gd.getLife(player2Id);

            when(gameQueryService.applyDamageMultiplier(gd, 1)).thenReturn(1);
            when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(player2Id), eq(1))).thenReturn(1);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player2Id), eq(1), any()))
                    .thenReturn(1);
            // canPlayerLifeChange defaults to false — life can't change

            registry.dispatch(
                    match(manabarbs, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(gd.getLife(player2Id)).isEqualTo(lifeBefore);
        }
    }

    // ===== ON_ANY_PLAYER_TAPS_LAND — AddManaOnEnchantedLandTapEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_TAPS_LAND — AddManaOnEnchantedLandTapEffect")
    class AddManaOnEnchantedLandTap {

        @Test
        @DisplayName("adds mana when enchanted land is tapped")
        void addsManaWhenEnchantedLandTapped() {
            Permanent overgrowth = createPermanent("Overgrowth");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            overgrowth.setAttachedTo(forest.getId());
            var effect = new AddManaOnEnchantedLandTapEffect(ManaColor.GREEN, 2);
            var ctx = new TriggerContext.LandTap(player1Id, forest.getId());

            int greenBefore = gd.playerManaPools.get(player1Id).get(ManaColor.GREEN);

            boolean result = registry.dispatch(
                    match(overgrowth, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isEqualTo(greenBefore + 2);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }

        @Test
        @DisplayName("returns false when a different land is tapped")
        void returnsFalseForDifferentLand() {
            Permanent overgrowth = createPermanent("Overgrowth");
            Permanent forest1 = createLandPermanent("Forest", ManaColor.GREEN);
            UUID differentLandId = UUID.randomUUID();
            overgrowth.setAttachedTo(forest1.getId());
            var effect = new AddManaOnEnchantedLandTapEffect(ManaColor.GREEN, 2);
            var ctx = new TriggerContext.LandTap(player1Id, differentLandId);

            int greenBefore = gd.playerManaPools.get(player1Id).get(ManaColor.GREEN);

            boolean result = registry.dispatch(
                    match(overgrowth, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isEqualTo(greenBefore);
        }

        @Test
        @DisplayName("returns false when permanent is not attached to anything")
        void returnsFalseWhenNotAttached() {
            Permanent overgrowth = createPermanent("Overgrowth");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new AddManaOnEnchantedLandTapEffect(ManaColor.GREEN, 2);
            var ctx = new TriggerContext.LandTap(player1Id, tappedLandId);

            boolean result = registry.dispatch(
                    match(overgrowth, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }
    }

    // ===== ON_ANY_PLAYER_TAPS_LAND — AddExtraManaOfChosenColorOnLandTapEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_TAPS_LAND — AddExtraManaOfChosenColorOnLandTapEffect")
    class AddExtraManaOfChosenColor {

        @Test
        @DisplayName("adds extra mana when controller's land produces chosen color")
        void addsExtraManaForChosenColor() {
            Permanent triggerPerm = createPermanent("Gauntlet of Power");
            triggerPerm.setChosenColor(CardColor.GREEN);
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            var effect = new AddExtraManaOfChosenColorOnLandTapEffect();
            var ctx = new TriggerContext.LandTap(player1Id, forest.getId());

            when(gameQueryService.findPermanentById(gd, forest.getId())).thenReturn(forest);

            int greenBefore = gd.playerManaPools.get(player1Id).get(ManaColor.GREEN);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isEqualTo(greenBefore + 1);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }

        @Test
        @DisplayName("returns false when opponent taps a land")
        void returnsFalseForOpponent() {
            Permanent triggerPerm = createPermanent("Gauntlet of Power");
            triggerPerm.setChosenColor(CardColor.GREEN);
            UUID tappedLandId = UUID.randomUUID();
            var effect = new AddExtraManaOfChosenColorOnLandTapEffect();
            var ctx = new TriggerContext.LandTap(player2Id, tappedLandId);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when no color is chosen")
        void returnsFalseWhenNoColorChosen() {
            Permanent triggerPerm = createPermanent("Gauntlet of Power");
            // chosenColor is null by default
            UUID tappedLandId = UUID.randomUUID();
            var effect = new AddExtraManaOfChosenColorOnLandTapEffect();
            var ctx = new TriggerContext.LandTap(player1Id, tappedLandId);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when tapped land is gone")
        void returnsFalseWhenLandGone() {
            Permanent triggerPerm = createPermanent("Gauntlet of Power");
            triggerPerm.setChosenColor(CardColor.GREEN);
            UUID missingLandId = UUID.randomUUID();
            var effect = new AddExtraManaOfChosenColorOnLandTapEffect();
            var ctx = new TriggerContext.LandTap(player1Id, missingLandId);

            when(gameQueryService.findPermanentById(gd, missingLandId)).thenReturn(null);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when tapped land does not produce chosen color")
        void returnsFalseWhenLandDoesNotProduceChosenColor() {
            Permanent triggerPerm = createPermanent("Gauntlet of Power");
            triggerPerm.setChosenColor(CardColor.GREEN);
            Permanent mountain = createLandPermanent("Mountain", ManaColor.RED);
            var effect = new AddExtraManaOfChosenColorOnLandTapEffect();
            var ctx = new TriggerContext.LandTap(player1Id, mountain.getId());

            when(gameQueryService.findPermanentById(gd, mountain.getId())).thenReturn(mountain);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }
    }

    // ===== ON_ANY_PLAYER_TAPS_LAND — AddOneOfEachManaTypeProducedByLandEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_TAPS_LAND — AddOneOfEachManaTypeProducedByLandEffect")
    class AddOneOfEachManaType {

        @Test
        @DisplayName("adds one additional mana of the type produced by the tapped land")
        void addsOneAdditionalMana() {
            Permanent triggerPerm = createPermanent("Mirari's Wake");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            var effect = new AddOneOfEachManaTypeProducedByLandEffect();
            var ctx = new TriggerContext.LandTap(player1Id, forest.getId());

            when(gameQueryService.findPermanentById(gd, forest.getId())).thenReturn(forest);

            int greenBefore = gd.playerManaPools.get(player1Id).get(ManaColor.GREEN);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isEqualTo(greenBefore + 1);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }

        @Test
        @DisplayName("returns false when opponent taps a land")
        void returnsFalseForOpponent() {
            Permanent triggerPerm = createPermanent("Mirari's Wake");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new AddOneOfEachManaTypeProducedByLandEffect();
            var ctx = new TriggerContext.LandTap(player2Id, tappedLandId);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when tapped land is gone")
        void returnsFalseWhenLandGone() {
            Permanent triggerPerm = createPermanent("Mirari's Wake");
            UUID missingLandId = UUID.randomUUID();
            var effect = new AddOneOfEachManaTypeProducedByLandEffect();
            var ctx = new TriggerContext.LandTap(player1Id, missingLandId);

            when(gameQueryService.findPermanentById(gd, missingLandId)).thenReturn(null);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when tapped land has no AwardManaEffect")
        void returnsFalseWhenLandHasNoManaEffect() {
            Permanent triggerPerm = createPermanent("Mirari's Wake");
            Permanent land = new Permanent(createLandCard("Maze of Ith"));
            var effect = new AddOneOfEachManaTypeProducedByLandEffect();
            var ctx = new TriggerContext.LandTap(player1Id, land.getId());

            when(gameQueryService.findPermanentById(gd, land.getId())).thenReturn(land);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }
    }

    // ===== ON_ANY_PLAYER_TAPS_LAND — OpponentTappedLandDoesntUntapEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_TAPS_LAND — OpponentTappedLandDoesntUntapEffect")
    class OpponentLandDoesntUntap {

        @Test
        @DisplayName("increments skipUntapCount on opponent's tapped land")
        void incrementsSkipUntapCount() {
            Permanent triggerPerm = createPermanent("Vorinclex, Voice of Hunger");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            var effect = new OpponentTappedLandDoesntUntapEffect();
            var ctx = new TriggerContext.LandTap(player2Id, forest.getId());

            when(gameQueryService.findPermanentById(gd, forest.getId())).thenReturn(forest);

            assertThat(forest.getSkipUntapCount()).isZero();

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(forest.getSkipUntapCount()).isEqualTo(1);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), any(String.class));
        }

        @Test
        @DisplayName("returns false when controller taps their own land")
        void returnsFalseForOwnLand() {
            Permanent triggerPerm = createPermanent("Vorinclex, Voice of Hunger");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new OpponentTappedLandDoesntUntapEffect();
            var ctx = new TriggerContext.LandTap(player1Id, tappedLandId);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns false when tapped land is gone")
        void returnsFalseWhenLandGone() {
            Permanent triggerPerm = createPermanent("Vorinclex, Voice of Hunger");
            UUID missingLandId = UUID.randomUUID();
            var effect = new OpponentTappedLandDoesntUntapEffect();
            var ctx = new TriggerContext.LandTap(player2Id, missingLandId);

            when(gameQueryService.findPermanentById(gd, missingLandId)).thenReturn(null);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }
    }
}
