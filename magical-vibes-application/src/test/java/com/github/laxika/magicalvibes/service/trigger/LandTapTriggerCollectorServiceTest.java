package com.github.laxika.magicalvibes.service.trigger;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddExtraManaOfChosenColorOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandOfColorTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandOfSubtypeTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentTappedLandDoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedChooseOpponentGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToPlayersEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Set;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LandTapTriggerCollectorServiceTest {

    @Test
    void additionalManaChoiceDoesNotReplaceAnEarlierChoice() {
        when(amountEvaluationService.evaluate(any(), any(), any())).thenReturn(1);
        Permanent aura = createPermanent("Mana Aura");
        Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
        aura.setAttachedTo(forest.getId());
        when(gameQueryService.findPermanentController(gd, forest.getId())).thenReturn(player1Id);
        var originalChoice = new PendingInteraction.ColorChoice(player1Id, null, null,
                new com.github.laxika.magicalvibes.model.ChoiceContext.ManaColorChoice(player1Id, false, 1),
                List.of("BLUE", "RED"), "Choose mana");
        gd.interaction.beginInteraction(originalChoice);
        var effect = new AddManaOnEnchantedLandTapEffect(new AwardAnyColorManaEffect());

        assertThat(registry.dispatch(match(aura, player1Id, effect), EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                effect, new TriggerContext.LandTap(player1Id, forest.getId()))).isTrue();

        assertThat(gd.interaction.activeInteraction()).isSameAs(originalChoice);
        assertThat(gd.pendingInteractions).hasSize(1);
        verify(interactionHandlerRegistry, org.mockito.Mockito.never()).begin(any(), any());
    }

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameLogService gameLogService;

    @Mock
    private InteractionHandlerRegistry interactionHandlerRegistry;

    @Mock
    private AmountEvaluationService amountEvaluationService;

    @Mock
    private PermanentControlSupport permanentControlSupport;

    @Mock
    private PredicateEvaluationService predicateEvaluationService;

    @Mock
    private LifeSupport lifeSupport;

    @Mock
    private ObjectProvider<DealDamageToPlayersEffectHandler> dealDamageHandlerProvider;

    @Mock
    private DealDamageToPlayersEffectHandler dealDamageHandler;

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

    @Test
    void controllerLandTapQueuesSourceCounterRemoval() {
        Permanent source = createPermanent("Savage Firecat");
        var effect = new RemoveCounterFromSourceEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);

        boolean result = registry.dispatch(match(source, player1Id, effect),
                EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, new TriggerContext.LandTap(player1Id, UUID.randomUUID()));

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(source.getId());
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
    }

    @Test
    void opponentLandTapDoesNotQueueSourceCounterRemoval() {
        Permanent source = createPermanent("Savage Firecat");
        var effect = new RemoveCounterFromSourceEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);

        boolean result = registry.dispatch(match(source, player1Id, effect),
                EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, new TriggerContext.LandTap(player2Id, UUID.randomUUID()));

        assertThat(result).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rainbow Vale's own tap queues its delayed control trigger")
    void rainbowValeTapQueuesDelayedControlTrigger() {
        Permanent source = createLandPermanent("Rainbow Vale", ManaColor.BLUE);
        var effect = new RegisterDelayedChooseOpponentGainsControlOfSourceEffect();

        boolean result = registry.dispatch(match(source, player1Id, effect),
                EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect,
                new TriggerContext.LandTap(player1Id, source.getId()));

        assertThat(result).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1Id);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(source.getId());
        assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(effect);
    }

    @Test
    @DisplayName("Rainbow Vale's trigger does not fire for another land's tap")
    void rainbowValeDoesNotTriggerForAnotherLand() {
        Permanent source = createLandPermanent("Rainbow Vale", ManaColor.BLUE);
        var effect = new RegisterDelayedChooseOpponentGainsControlOfSourceEffect();

        boolean result = registry.dispatch(match(source, player1Id, effect),
                EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect,
                new TriggerContext.LandTap(player1Id, UUID.randomUUID()));

        assertThat(result).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Nested
    @DisplayName("ON_ANY_PLAYER_TAPS_LAND — DealDamageOnLandTapEffect")
    class DealDamageOnLandTap {

        @Test
        @DisplayName("Queues damage to the player who tapped the land")
        void queuesDamageToTappingPlayer() {
            Permanent manabarbs = createPermanent("Manabarbs");
            var effect = new DealDamageOnLandTapEffect(1);

            boolean result = registry.dispatch(
                    match(manabarbs, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect,
                    new TriggerContext.LandTap(player2Id, UUID.randomUUID()));

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            var entry = gd.stack.getFirst();
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getTargetId()).isEqualTo(player2Id);
            assertThat(entry.getSourcePermanentId()).isEqualTo(manabarbs.getId());
            assertThat(entry.getSourcePermanentSnapshot().getId()).isEqualTo(manabarbs.getId());
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getEffectsToResolve()).containsExactly(
                    new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER));
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }

        @Test
        @DisplayName("Also queues damage when the source controller taps a land")
        void queuesDamageToController() {
            Permanent manabarbs = createPermanent("Manabarbs");
            var effect = new DealDamageOnLandTapEffect(1);

            boolean result = registry.dispatch(
                    match(manabarbs, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect,
                    new TriggerContext.LandTap(player1Id, UUID.randomUUID()));

            assertThat(result).isTrue();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("Does not queue damage when the tapped land fails the filter")
        void ignoresLandThatFailsFilter() {
            Permanent burningEarth = createPermanent("Burning Earth");
            Permanent tappedLand = createLandPermanent("Snow-Covered Island", ManaColor.BLUE);
            var filter = new PermanentHasSupertypePredicate(CardSupertype.SNOW);
            var effect = new DealDamageOnLandTapEffect(1, filter);
            when(gameQueryService.findPermanentById(gd, tappedLand.getId())).thenReturn(tappedLand);
            when(predicateEvaluationService.matchesPermanentPredicate(gd, tappedLand, filter)).thenReturn(false);

            boolean result = registry.dispatch(
                    match(burningEarth, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect,
                    new TriggerContext.LandTap(player2Id, tappedLand.getId()));

            assertThat(result).isFalse();
            assertThat(gd.stack).isEmpty();
        }
    }

    @Nested
    @DisplayName("ON_ANY_PLAYER_TAPS_LAND — AddManaOnEnchantedLandTapEffect")
    class AddManaOnEnchantedLandTap {

        @Test
        @DisplayName("adds mana when enchanted land is tapped")
        void addsManaWhenEnchantedLandTapped() {
            Permanent overgrowth = createPermanent("Overgrowth");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            overgrowth.setAttachedTo(forest.getId());
            when(gameQueryService.findPermanentController(gd, forest.getId())).thenReturn(player1Id);
            var effect = new AddManaOnEnchantedLandTapEffect(new AwardManaEffect(ManaColor.GREEN, 2));
            var ctx = new TriggerContext.LandTap(player1Id, forest.getId());

            when(amountEvaluationService.evaluate(any(), any(), any())).thenReturn(2);

            int greenBefore = gd.playerManaPools.get(player1Id).get(ManaColor.GREEN);

            boolean result = registry.dispatch(
                    match(overgrowth, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isEqualTo(greenBefore + 2);
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }

        @Test
        @DisplayName("returns false when a different land is tapped")
        void returnsFalseForDifferentLand() {
            Permanent overgrowth = createPermanent("Overgrowth");
            Permanent forest1 = createLandPermanent("Forest", ManaColor.GREEN);
            UUID differentLandId = UUID.randomUUID();
            overgrowth.setAttachedTo(forest1.getId());
            var effect = new AddManaOnEnchantedLandTapEffect(new AwardManaEffect(ManaColor.GREEN, 2));
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
            var effect = new AddManaOnEnchantedLandTapEffect(new AwardManaEffect(ManaColor.GREEN, 2));
            var ctx = new TriggerContext.LandTap(player1Id, tappedLandId);

            boolean result = registry.dispatch(
                    match(overgrowth, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("begins color choice for any-color mana when enchanted land is tapped")
        void beginsColorChoiceForAnyColorMana() {
            when(amountEvaluationService.evaluate(any(), any(), any())).thenReturn(1);
            Permanent fertileGround = createPermanent("Fertile Ground");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            fertileGround.setAttachedTo(forest.getId());
            when(gameQueryService.findPermanentController(gd, forest.getId())).thenReturn(player1Id);
            var effect = new AddManaOnEnchantedLandTapEffect(new AwardAnyColorManaEffect());
            var ctx = new TriggerContext.LandTap(player1Id, forest.getId());

            boolean result = registry.dispatch(
                    match(fertileGround, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            verify(interactionHandlerRegistry).begin(eq(gd),
                    any(com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice.class));
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }

        @Test
        @DisplayName("begins one color choice per mana for any-combination mana")
        void beginsColorChoiceForAnyCombinationMana() {
            when(amountEvaluationService.evaluate(any(), any(), any())).thenReturn(2);
            Permanent dawnsReflection = createPermanent("Dawn's Reflection");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            dawnsReflection.setAttachedTo(forest.getId());
            when(gameQueryService.findPermanentController(gd, forest.getId())).thenReturn(player1Id);
            var effect = new AddManaOnEnchantedLandTapEffect(
                    new AwardManaOfColorsEffect(ManaColor.COLORS, 2));
            var ctx = new TriggerContext.LandTap(player1Id, forest.getId());

            boolean result = registry.dispatch(
                    match(dawnsReflection, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            verify(interactionHandlerRegistry).begin(eq(gd),
                    any(PendingInteraction.ColorChoice.class));
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
            when(gameQueryService.findPermanentController(gd, forest.getId())).thenReturn(player1Id);

            int greenBefore = gd.playerManaPools.get(player1Id).get(ManaColor.GREEN);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isEqualTo(greenBefore + 1);
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
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

        @Test
        @DisplayName("symmetric filtered form adds extra mana for another player's matching land")
        void addsExtraManaForMatchingLandSymmetrically() {
            Permanent triggerPerm = createPermanent("Gauntlet of Power");
            triggerPerm.setChosenColor(CardColor.GREEN);
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            var filter = new PermanentHasSupertypePredicate(CardSupertype.BASIC);
            var effect = new AddExtraManaOfChosenColorOnLandTapEffect(false, filter);
            var ctx = new TriggerContext.LandTap(player2Id, forest.getId());

            when(gameQueryService.findPermanentById(gd, forest.getId())).thenReturn(forest);
            when(gameQueryService.findPermanentController(gd, forest.getId())).thenReturn(player2Id);
            when(predicateEvaluationService.matchesPermanentPredicate(gd, forest, filter)).thenReturn(true);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.GREEN)).isEqualTo(1);
        }

        @Test
        @DisplayName("filtered form ignores a nonmatching land")
        void ignoresNonmatchingLand() {
            Permanent triggerPerm = createPermanent("Gauntlet of Power");
            triggerPerm.setChosenColor(CardColor.GREEN);
            Permanent land = createLandPermanent("Gaea's Cradle", ManaColor.GREEN);
            var filter = new PermanentHasSupertypePredicate(CardSupertype.BASIC);
            var effect = new AddExtraManaOfChosenColorOnLandTapEffect(false, filter);
            var ctx = new TriggerContext.LandTap(player2Id, land.getId());

            when(gameQueryService.findPermanentById(gd, land.getId())).thenReturn(land);
            when(predicateEvaluationService.matchesPermanentPredicate(gd, land, filter)).thenReturn(false);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.GREEN)).isZero();
        }
    }

    // ===== ON_ANY_PLAYER_TAPS_LAND — AddOneOfEachManaTypeProducedByLandEffect =====

    @Nested
    @DisplayName("ON_ANY_PLAYER_TAPS_LAND — AddOneOfEachManaTypeProducedByLandEffect")
    class AddOneOfEachManaType {

        @Test
        void waitsForTheLandsManaColorChoice() {
            Permanent source = createPermanent("Mana Flare");
            Permanent land = new Permanent(createLandCard("City of Brass"));
            var effect = new AddOneOfEachManaTypeProducedByLandEffect(false);
            when(gameQueryService.findPermanentById(gd, land.getId())).thenReturn(land);
            gd.interaction.beginInteraction(new PendingInteraction.ColorChoice(
                    player2Id, null, null, null, List.of("RED"), "Choose mana."));

            boolean result = registry.dispatch(match(source, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect,
                    new TriggerContext.LandTap(player2Id, land.getId()));

            assertThat(result).isTrue();
            assertThat(gd.pendingManaAbilityTriggers).hasSize(1);
            assertThat(gd.pendingManaAbilityTriggers.getFirst().getControllerId()).isEqualTo(player2Id);
            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.RED)).isZero();
        }

        @Test
        @DisplayName("resolves a combined mana-and-damage ability immediately")
        void resolvesCombinedManaAndDamageAbilityImmediately() {
            Permanent triggerPerm = createPermanent("Overabundance");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            var effect = SequenceEffect.of(
                    new AddOneOfEachManaTypeProducedByLandEffect(false),
                    new DealDamageOnLandTapEffect(1));
            var ctx = new TriggerContext.LandTap(player2Id, forest.getId());

            when(gameQueryService.findPermanentById(gd, forest.getId())).thenReturn(forest);
            when(dealDamageHandlerProvider.getObject()).thenReturn(dealDamageHandler);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(gd.stack).isEmpty();
            verify(dealDamageHandler).resolve(
                    eq(gd), any(StackEntry.class), eq(new DealDamageToPlayersEffect(
                            1, DamageRecipient.TARGET_PLAYER)));
        }

        @Test
        @DisplayName("adds one additional mana of the type produced by the tapped land")
        void addsOneAdditionalMana() {
            Permanent triggerPerm = createPermanent("Mirari's Wake");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            var effect = new AddOneOfEachManaTypeProducedByLandEffect(true);
            var ctx = new TriggerContext.LandTap(player1Id, forest.getId());

            when(gameQueryService.findPermanentById(gd, forest.getId())).thenReturn(forest);

            int greenBefore = gd.playerManaPools.get(player1Id).get(ManaColor.GREEN);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isEqualTo(greenBefore + 1);
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }

        @Test
        @DisplayName("returns false when opponent taps a land")
        void returnsFalseForOpponent() {
            Permanent triggerPerm = createPermanent("Mirari's Wake");
            UUID tappedLandId = UUID.randomUUID();
            var effect = new AddOneOfEachManaTypeProducedByLandEffect(true);
            var ctx = new TriggerContext.LandTap(player2Id, tappedLandId);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("symmetric (controllerOnly=false): opponent's land tap adds mana to that player")
        void symmetricAddsManaForOpponent() {
            Permanent triggerPerm = createPermanent("Mana Flare");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            var effect = new AddOneOfEachManaTypeProducedByLandEffect(false);
            var ctx = new TriggerContext.LandTap(player2Id, forest.getId());

            when(gameQueryService.findPermanentById(gd, forest.getId())).thenReturn(forest);

            int greenBefore = gd.playerManaPools.get(player2Id).get(ManaColor.GREEN);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.GREEN)).isEqualTo(greenBefore + 1);
        }

        @Test
        @DisplayName("returns false when tapped land is gone")
        void returnsFalseWhenLandGone() {
            Permanent triggerPerm = createPermanent("Mirari's Wake");
            UUID missingLandId = UUID.randomUUID();
            var effect = new AddOneOfEachManaTypeProducedByLandEffect(true);
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
            var effect = new AddOneOfEachManaTypeProducedByLandEffect(true);
            var ctx = new TriggerContext.LandTap(player1Id, land.getId());

            when(gameQueryService.findPermanentById(gd, land.getId())).thenReturn(land);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("AddManaWhenLandOfSubtypeTappedForManaEffect")
    class AddManaWhenLandOfSubtypeTappedForMana {

        @Test
        @DisplayName("controller-only form adds mana when the source controller taps a matching land")
        void controllerOnlyAddsManaForController() {
            Permanent triggerPerm = createPermanent("Crypt Ghast");
            Card swampCard = createLandCardWithMana("Swamp", ManaColor.BLACK);
            swampCard.setSubtypes(List.of(CardSubtype.SWAMP));
            Permanent swamp = new Permanent(swampCard);
            var effect = new AddManaWhenLandOfSubtypeTappedForManaEffect(
                    CardSubtype.SWAMP, ManaColor.BLACK, true);
            var ctx = new TriggerContext.LandTap(player1Id, swamp.getId());

            when(gameQueryService.findPermanentById(gd, swamp.getId())).thenReturn(swamp);
            when(gameQueryService.hasEffectiveSubtype(gd, swamp, CardSubtype.SWAMP)).thenReturn(true);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.BLACK)).isEqualTo(1);
        }

        @Test
        @DisplayName("controller-only form ignores an opponent's matching land")
        void controllerOnlyIgnoresOpponent() {
            Permanent triggerPerm = createPermanent("Crypt Ghast");
            Card swampCard = createLandCardWithMana("Swamp", ManaColor.BLACK);
            swampCard.setSubtypes(List.of(CardSubtype.SWAMP));
            Permanent swamp = new Permanent(swampCard);
            var effect = new AddManaWhenLandOfSubtypeTappedForManaEffect(
                    CardSubtype.SWAMP, ManaColor.BLACK, true);
            var ctx = new TriggerContext.LandTap(player2Id, swamp.getId());

            when(gameQueryService.findPermanentById(gd, swamp.getId())).thenReturn(swamp);
            when(gameQueryService.hasEffectiveSubtype(gd, swamp, CardSubtype.SWAMP)).thenReturn(true);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.BLACK)).isZero();
        }

        @Test
        @DisplayName("default form remains symmetric")
        void defaultFormRemainsSymmetric() {
            Permanent triggerPerm = createPermanent("Vernal Bloom");
            Card swampCard = createLandCardWithMana("Swamp", ManaColor.BLACK);
            swampCard.setSubtypes(List.of(CardSubtype.SWAMP));
            Permanent swamp = new Permanent(swampCard);
            var effect = new AddManaWhenLandOfSubtypeTappedForManaEffect(
                    CardSubtype.SWAMP, ManaColor.BLACK);
            var ctx = new TriggerContext.LandTap(player2Id, swamp.getId());

            when(gameQueryService.findPermanentById(gd, swamp.getId())).thenReturn(swamp);
            when(gameQueryService.hasEffectiveSubtype(gd, swamp, CardSubtype.SWAMP)).thenReturn(true);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.BLACK)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("AddManaWhenLandOfColorTappedForManaEffect")
    class AddManaWhenLandOfColorTappedForMana {

        @Test
        @DisplayName("adds colorless mana when the controller taps a land for colorless mana")
        void addsManaForControllerLand() {
            Permanent triggerPerm = createPermanent("Ultima, Origin of Oblivion");
            Permanent land = createLandPermanent("Wastes", ManaColor.COLORLESS);
            var effect = new AddManaWhenLandOfColorTappedForManaEffect(ManaColor.COLORLESS);
            var ctx = new TriggerContext.LandTap(player1Id, land.getId());

            when(gameQueryService.findPermanentById(gd, land.getId())).thenReturn(land);

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.COLORLESS)).isOne();
        }

        @Test
        @DisplayName("does not add mana when an opponent taps a land")
        void ignoresOpponentLand() {
            Permanent triggerPerm = createPermanent("Ultima, Origin of Oblivion");
            Permanent land = createLandPermanent("Wastes", ManaColor.COLORLESS);
            var effect = new AddManaWhenLandOfColorTappedForManaEffect(ManaColor.COLORLESS);
            var ctx = new TriggerContext.LandTap(player2Id, land.getId());

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.COLORLESS)).isZero();
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
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
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

    @Nested
    @DisplayName("ON_ANY_PLAYER_TAPS_LAND — CreateTokenForTargetPlayerEffect")
    class CreateTokenForOpponentOnSelfTapped {

        private final CreateTokenForTargetPlayerEffect effect = new CreateTokenForTargetPlayerEffect(
                new CreateTokenEffect("Spirit", 1, 1, null, List.of(CardSubtype.SPIRIT), Set.of(), Set.of()));

        @Test
        @DisplayName("creates the token for the opponent when the source land itself is tapped")
        void createsTokenForOpponent() {
            Permanent orchard = createLandPermanent("Forbidden Orchard", ManaColor.COLORLESS);
            var ctx = new TriggerContext.LandTap(player1Id, orchard.getId());

            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

            boolean result = registry.dispatch(
                    match(orchard, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            verify(permanentControlSupport).applyCreateToken(eq(gd), eq(player2Id), eq(effect.tokenEffect()), any());
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }

        @Test
        @DisplayName("does not trigger when a different land is tapped")
        void ignoresOtherLands() {
            Permanent orchard = createLandPermanent("Forbidden Orchard", ManaColor.COLORLESS);
            var ctx = new TriggerContext.LandTap(player1Id, UUID.randomUUID());

            boolean result = registry.dispatch(
                    match(orchard, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("does not trigger when an opponent taps it (Piracy)")
        void ignoresOpponentTap() {
            Permanent orchard = createLandPermanent("Forbidden Orchard", ManaColor.COLORLESS);
            var ctx = new TriggerContext.LandTap(player2Id, orchard.getId());

            boolean result = registry.dispatch(
                    match(orchard, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("AddManaWhenLandTappedForManaEffect")
    class AddManaWhenLandTappedForMana {

        @Test
        @DisplayName("adds fixed-color mana when the source controller taps a land")
        void addsManaForControllerLand() {
            Permanent triggerPerm = createPermanent("Groundchuck & Dirtbag");
            var effect = new AddManaWhenLandTappedForManaEffect(ManaColor.GREEN);
            var ctx = new TriggerContext.LandTap(player1Id, UUID.randomUUID());

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isTrue();
            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isOne();
        }

        @Test
        @DisplayName("does not add fixed-color mana when an opponent taps a land")
        void ignoresOpponentLand() {
            Permanent triggerPerm = createPermanent("Groundchuck & Dirtbag");
            var effect = new AddManaWhenLandTappedForManaEffect(ManaColor.GREEN);
            var ctx = new TriggerContext.LandTap(player2Id, UUID.randomUUID());

            boolean result = registry.dispatch(
                    match(triggerPerm, player1Id, effect),
                    EffectSlot.ON_ANY_PLAYER_TAPS_LAND, effect, ctx);

            assertThat(result).isFalse();
            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.GREEN)).isZero();
        }
    }
}
