package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.AddExtraManaOfChosenColorOnLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaOfTypeProducedByTappedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandOfColorTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.normalfx.AddExtraManaOfChosenColorOnLandTapEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.AddManaOfTypeProducedByTappedPermanentEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.AddManaOnEnchantedLandTapEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.AddManaWhenLandOfColorTappedForManaEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.AddManaWhenTappedForManaEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.AddOneOfEachManaTypeProducedByLandEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DealDamageToPlayersEffectHandler;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The tap-for-mana abilities' one default ({@link TapTriggers}): every ability's entry records the
 * tap whatever its effects are; a triggered mana ability resolves at once through its effects' handlers (CR 605.4a), a
 * targeted one has its target chosen with the same record (CR 603.3d), any other goes on the stack. The trigger, its
 * relation and its filters are judged by the caller ({@code TriggerCollectionService}), not here.
 */
@ExtendWith(MockitoExtension.class)
class TapTriggersTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameLogService gameLogService;

    @Mock
    private InteractionHandlerRegistry interactionHandlerRegistry;

    @Mock
    private AmountEvaluationService amountEvaluationService;

    @Mock
    private DealDamageToPlayersEffectHandler dealDamageHandler;

    @Mock
    private TriggerCollectionService triggerCollectionService;

    @Mock
    private ConditionEvaluationService conditionEvaluationService;

    @Mock
    private PredicateEvaluationService predicateEvaluationService;

    @Mock
    private GraveyardTargetingSupport graveyardTargetingSupport;

    @Mock
    private TriggerTargetCollector triggerTargetCollector;

    @Spy
    private EffectHandlerRegistry effectHandlerRegistry = new EffectHandlerRegistry();

    private TapTriggers sut;
    private AddOneOfEachManaTypeProducedByLandEffectHandler addOneOfEachHandler;
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
        gd.playerIds.addAll(List.of(player1Id, player2Id));
        TriggeredAbilityStackService door = new TriggeredAbilityStackService(conditionEvaluationService,
                gameQueryService, graveyardTargetingSupport, gameLogService, triggerCollectionService,
                amountEvaluationService, predicateEvaluationService, triggerTargetCollector);
        sut = new TapTriggers(null, gameQueryService, predicateEvaluationService, conditionEvaluationService,
                gameLogService, null, effectHandlerRegistry, door, triggerCollectionService);
        addOneOfEachHandler = new AddOneOfEachManaTypeProducedByLandEffectHandler(
                gameQueryService, gameLogService, interactionHandlerRegistry);
        effectHandlerRegistry.register(AddOneOfEachManaTypeProducedByLandEffect.class, addOneOfEachHandler);
        effectHandlerRegistry.register(AddManaOnEnchantedLandTapEffect.class, new AddManaOnEnchantedLandTapEffectHandler(
                gameQueryService, gameLogService, amountEvaluationService, interactionHandlerRegistry));
        effectHandlerRegistry.register(AddExtraManaOfChosenColorOnLandTapEffect.class,
                new AddExtraManaOfChosenColorOnLandTapEffectHandler(gameQueryService, gameLogService));
        effectHandlerRegistry.register(AddManaWhenTappedForManaEffect.class,
                new AddManaWhenTappedForManaEffectHandler(gameQueryService, gameLogService));
        effectHandlerRegistry.register(AddManaWhenLandOfColorTappedForManaEffect.class,
                new AddManaWhenLandOfColorTappedForManaEffectHandler(gameQueryService, gameLogService));
        effectHandlerRegistry.register(AddManaOfTypeProducedByTappedPermanentEffect.class,
                new AddManaOfTypeProducedByTappedPermanentEffectHandler(gameLogService, interactionHandlerRegistry));
        effectHandlerRegistry.register(DealDamageToPlayersEffect.class, dealDamageHandler);
    }

    private static Card createCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    /**
     * A land that taps for {@code color}: a basic land type gives a colored one its mana ability (CR 305.6), and a
     * colorless one prints "{T}: Add {C}." like Wastes.
     */
    private static Permanent createLandPermanent(String name, ManaColor color) {
        Card card = createCard(name, CardType.LAND);
        switch (color) {
            case WHITE -> card.setSubtypes(List.of(CardSubtype.PLAINS));
            case BLUE -> card.setSubtypes(List.of(CardSubtype.ISLAND));
            case BLACK -> card.setSubtypes(List.of(CardSubtype.SWAMP));
            case RED -> card.setSubtypes(List.of(CardSubtype.MOUNTAIN));
            case GREEN -> card.setSubtypes(List.of(CardSubtype.FOREST));
            default -> card.addActivatedAbility(ManaAbilities.tapFor(color));
        }
        return new Permanent(card);
    }

    private static Permanent createPermanent(String name) {
        return new Permanent(createCard(name, CardType.ENCHANTMENT));
    }

    private boolean tapLand(Permanent source, UUID controllerId, CardEffect effect, UUID tappingPlayerId,
                            UUID landId, ManaColor... produced) {
        return sut.collect(gd, source(source, controllerId, effect), tappingPlayerId, landId, Set.of(produced),
                tappingPlayerId, false);
    }

    private static TapTriggers.Source source(Permanent source, UUID controllerId, CardEffect effect) {
        return new TapTriggers.Source(source, controllerId, List.of(effect), source == null ? null : source.getCard(),
                null);
    }

    @Nested
    @DisplayName("an ability that uses the stack")
    class OnTheStack {

        @Test
        @DisplayName("records the tapping player as \"that player\", the land as \"that land\" and the source")
        void recordsTheTap() {
            Permanent manabarbs = createPermanent("Manabarbs");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            when(gameQueryService.findPermanentById(gd, forest.getId())).thenReturn(forest);
            var effect = new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER);

            assertThat(tapLand(manabarbs, player1Id, effect, player2Id, forest.getId(), ManaColor.GREEN)).isTrue();

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getControllerId()).isEqualTo(player1Id);
            assertThat(entry.getEffectsToResolve()).containsExactly(effect);
            assertThat(entry.getTargetId()).isEqualTo(player2Id);
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id);
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(forest.getId());
            assertThat(entry.getTriggeringPermanentSnapshot().getId()).isEqualTo(forest.getId());
            assertThat(entry.getProducedManaColors()).containsExactly(ManaColor.GREEN);
            assertThat(entry.getSourcePermanentId()).isEqualTo(manabarbs.getId());
            assertThat(entry.getSourcePermanentSnapshot().getId()).isEqualTo(manabarbs.getId());
            verify(gameLogService).append(eq(gd), any(GameLogEntry.class));
        }

        @Test
        @DisplayName("Savage Firecat's counter removal goes on the stack as written, whatever its effect is")
        void counterRemovalGoesOnTheStack() {
            Permanent firecat = createPermanent("Savage Firecat");
            UUID landId = UUID.randomUUID();
            var effect = new RemoveCounterFromSourceEffect(CounterType.PLUS_ONE_PLUS_ONE, 1);

            assertThat(tapLand(firecat, player1Id, effect, player1Id, landId)).isTrue();

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(firecat.getId());
            assertThat(gd.stack.getFirst().getTriggeringPermanentId()).isEqualTo(landId);
            assertThat(gd.stack.getFirst().getTriggeringPlayerId()).isEqualTo(player1Id);
        }

        @Test
        @DisplayName("\"that land doesn't untap\" waits on the stack for the land as it resolves")
        void skipUntapWaitsOnTheStack() {
            Permanent vorinclex = createPermanent("Vorinclex, Voice of Hunger");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            var effect = new SkipNextUntapEffect(TapUntapScope.TRIGGERING);

            assertThat(tapLand(vorinclex, player1Id, effect, player2Id, forest.getId())).isTrue();

            assertThat(forest.getSkipUntapCount()).isZero();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTriggeringPermanentId()).isEqualTo(forest.getId());
        }

        @Test
        @DisplayName("a land that left as its mana ability resolved is recorded as it last existed (Mana Web's \"that land\")")
        void departedLandIsRecordedAsItLastExisted() {
            Permanent manaWeb = createPermanent("Mana Web");
            Permanent mine = createLandPermanent("Gemstone Mine", ManaColor.COLORLESS);
            gd.departedPermanentSnapshots.put(mine.getCard().getId(), mine);
            var effect = new GainLifeEffect(1);

            assertThat(tapLand(manaWeb, player1Id, effect, player2Id, mine.getId())).isTrue();

            assertThat(gd.stack.getFirst().getTriggeringPermanentSnapshot().getId()).isEqualTo(mine.getId());
        }

        @Test
        @DisplayName("an ability that uses the stack keeps its intervening \"if\" for the check as it resolves")
        void nonManaAbilityKeepsItsInterveningIf() {
            var damage = new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER);
            var ability = new TriggeredAbility(Triggers.playerTapsLandForMana(),
                    TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped()), damage);

            CardEffect effect = TapTriggers.read(ability, Triggers.playerTapsLandForMana())
                    .getFirst().effectAsJudged();

            assertThat(TapTriggers.isTriggeredManaAbility(ability)).isFalse();
            assertThat(effect).isEqualTo(new ConditionalEffect(new SourceIsTapped(), damage));
        }
    }

    @Nested
    @DisplayName("a targeted ability")
    class Targeted {

        @Test
        @DisplayName("Forbidden Orchard's declared \"target opponent\" is chosen at the stack door with the record of the tap")
        void playerTargetIsChosenWithTheRecord() {
            Card orchardCard = createCard("Forbidden Orchard", CardType.LAND);
            var effect = new CreateTokenForTargetPlayerEffect(
                    new CreateTokenEffect("Spirit", 1, 1, null, List.of(CardSubtype.SPIRIT), Set.of(), Set.of()),
                    PlayerRelation.OPPONENT);
            orchardCard.target(new PlayerPredicateTargetFilter(
                    new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent"))
                    .addTrigger(Triggers.youTapThisLandForMana(), effect);
            Permanent orchard = new Permanent(orchardCard);

            assertThat(tapLand(orchard, player1Id, effect, player1Id, orchard.getId(), ManaColor.GREEN)).isTrue();

            assertThat(gd.stack).isEmpty();
            var choice = (PermanentChoiceContext.AttackTriggerTarget) gd.pendingInteractions.getFirst();
            assertThat(choice.effects()).containsExactly(effect);
            assertThat(choice.sourcePermanentId()).isEqualTo(orchard.getId());
            StackEntry record = choice.triggerRecord();
            assertThat(record.isParkedAtStackDoor()).isTrue();
            assertThat(record.getTriggeringPlayerId()).isEqualTo(player1Id);
            assertThat(record.getTriggeringPermanentId()).isEqualTo(orchard.getId());
            assertThat(record.getProducedManaColors()).containsExactly(ManaColor.GREEN);
        }

        @Test
        @DisplayName("C.A.M.P.'s target waits for the land's mana choice and takes the chosen color into its choice")
        void campWaitsForTheManaColor() {
            Permanent camp = createPermanent("C.A.M.P.");
            Permanent land = new Permanent(createCard("City of Brass", CardType.LAND));
            camp.setAttachedTo(land.getId());
            var effect = new PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect(
                    new CreateTokenEffect("Junk", 0, 0, null, List.of(), Set.of(), Set.of()));
            gd.interaction.beginInteraction(new PendingInteraction.ColorChoice(
                    player1Id, null, null, null, List.of("RED"), "Choose mana."));

            assertThat(tapLand(camp, player1Id, effect, player1Id, land.getId())).isTrue();

            StackEntry parked = gd.pendingManaAbilityTriggers.getFirst();
            assertThat(parked.isAwaitingProducedManaColor()).isTrue();
            assertThat(parked.getEffectsToResolve()).containsExactly(effect);
            assertThat(parked.getTargetFilter()).isNull();

            gd.interaction.clearAwaitingInput();
            gd.pendingManaAbilityTriggers.remove(parked);
            assertThat(sut.putResolvedProducedManaTrigger(gd, parked, ManaColor.RED)).isTrue();

            var choice = (PermanentChoiceContext.AttackTriggerTarget) gd.pendingInteractions.getFirst();
            assertThat(choice.effects()).containsExactly(effect);
            StackEntry record = choice.triggerRecord();
            assertThat(record.isParkedAtStackDoor()).isTrue();
            assertThat(record.getProducedManaColors()).containsExactly(ManaColor.RED);
            assertThat(record.getTriggeringPermanentId()).isEqualTo(land.getId());
            // "Target creature you control" is the effect's own target, not a filter the collector adds.
            assertThat(record.getTargetFilter()).isNull();
            verify(triggerCollectionService).beginStackDoorChoices(gd);
        }

        @Test
        @DisplayName("a parked ability whose effects target nothing is not routed to a target choice")
        void parkedUntargetedAbilityIsNotRoutedToATargetChoice() {
            Card source = createCard("Mana Trigger Source", CardType.ENCHANTMENT);
            StackEntry parked = StackEntry.carrier(source, player1Id, "Mana Trigger Source's ability",
                    List.of(new GainLifeEffect(1)));

            assertThat(sut.putResolvedProducedManaTrigger(gd, parked, ManaColor.RED)).isFalse();

            assertThat(gd.pendingInteractions).isEmpty();
        }
    }

    @Nested
    @DisplayName("a triggered mana ability resolves at once through its effects' handlers")
    class TriggeredManaAbility {

        @Test
        @DisplayName("an enchanted land's fixed mana goes to the land's controller, not to the Aura's controller")
        void enchantedLandManaGoesToTheLandsController() {
            when(amountEvaluationService.evaluate(any(), any(), any())).thenReturn(2);
            Permanent overgrowth = createPermanent("Overgrowth");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            when(gameQueryService.findPermanentController(gd, forest.getId())).thenReturn(player2Id);
            var effect = new AddManaOnEnchantedLandTapEffect(new AwardManaEffect(ManaColor.GREEN, 2));

            assertThat(tapLand(overgrowth, player1Id, effect, player2Id, forest.getId())).isTrue();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.GREEN)).isEqualTo(2);
            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isZero();
        }

        @Test
        @DisplayName("an enchanted land's \"any color\" begins the tapping player's color choice")
        void enchantedLandAnyColorBeginsTheChoice() {
            when(amountEvaluationService.evaluate(any(), any(), any())).thenReturn(1);
            Permanent fertileGround = createPermanent("Fertile Ground");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            var effect = new AddManaOnEnchantedLandTapEffect(new AwardAnyColorManaEffect());

            assertThat(tapLand(fertileGround, player1Id, effect, player1Id, forest.getId())).isTrue();

            verify(interactionHandlerRegistry).begin(eq(gd), any());
        }

        @Test
        @DisplayName("an additional mana choice doesn't replace an earlier choice: it waits behind it")
        void additionalManaChoiceDoesNotReplaceAnEarlierChoice() {
            when(amountEvaluationService.evaluate(any(), any(), any())).thenReturn(1);
            Permanent aura = createPermanent("Mana Aura");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            var originalChoice = new PendingInteraction.ColorChoice(player1Id, null, null,
                    new com.github.laxika.magicalvibes.model.ChoiceContext.ManaColorChoice(player1Id, false, 1),
                    List.of("BLUE", "RED"), "Choose mana");
            gd.interaction.beginInteraction(originalChoice);
            var effect = new AddManaOnEnchantedLandTapEffect(new AwardManaOfColorsEffect(ManaColor.COLORS, 1));

            assertThat(tapLand(aura, player1Id, effect, player1Id, forest.getId(), ManaColor.GREEN)).isTrue();

            assertThat(gd.interaction.activeInteraction()).isSameAs(originalChoice);
            assertThat(gd.pendingInteractions).hasSize(1);
            verify(interactionHandlerRegistry, org.mockito.Mockito.never()).begin(any(), any());
        }

        @Test
        @DisplayName("Caged Sun adds one more of the chosen color when the land produced it, and nothing otherwise")
        void chosenColorExtraManaReadsTheProducedMana() {
            Permanent cagedSun = createPermanent("Caged Sun");
            cagedSun.setChosenColor(CardColor.GREEN);
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            Permanent mountain = createLandPermanent("Mountain", ManaColor.RED);
            when(gameQueryService.findPermanentById(gd, forest.getId())).thenReturn(forest);
            when(gameQueryService.findPermanentById(gd, mountain.getId())).thenReturn(mountain);
            when(gameQueryService.findPermanentById(gd, cagedSun.getId())).thenReturn(cagedSun);
            var effect = new AddExtraManaOfChosenColorOnLandTapEffect();

            tapLand(cagedSun, player1Id, effect, player1Id, forest.getId(), ManaColor.GREEN);
            tapLand(cagedSun, player1Id, effect, player1Id, mountain.getId(), ManaColor.RED);

            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.RED)).isZero();
        }

        @Test
        @DisplayName("\"its controller adds an additional {G}\" goes to the land's controller, not to who tapped it (Vernal Bloom)")
        void itsControllerAddsGoesToTheLandsController() {
            Permanent vernalBloom = createPermanent("Vernal Bloom");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            when(gameQueryService.findPermanentController(gd, forest.getId())).thenReturn(player2Id);
            var effect = AddManaWhenTappedForManaEffect.ofItsController(ManaColor.GREEN);

            tapLand(vernalBloom, player1Id, effect, player1Id, forest.getId(), ManaColor.GREEN);

            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isZero();
            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.GREEN)).isEqualTo(1);
        }

        @Test
        @DisplayName("\"add an additional {B}\" goes to the ability's controller (Crypt Ghast)")
        void addGoesToTheAbilitysController() {
            Permanent cryptGhast = createPermanent("Crypt Ghast");
            Permanent swamp = createLandPermanent("Swamp", ManaColor.BLACK);
            var effect = new AddManaWhenTappedForManaEffect(ManaColor.BLACK);

            tapLand(cryptGhast, player1Id, effect, player1Id, swamp.getId(), ManaColor.BLACK);

            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.BLACK)).isEqualTo(1);
        }

        @Test
        @DisplayName("\"a land tapped for {C}\" adds {C} for a land on the battlefield that produced it, else nothing")
        void colorTapBonusReadsTheProducedMana() {
            Permanent ultima = createPermanent("Ultima, Origin of Oblivion");
            Permanent wastes = createLandPermanent("Wastes", ManaColor.COLORLESS);
            when(gameQueryService.findPermanentById(gd, wastes.getId())).thenReturn(wastes);
            var effect = new AddManaWhenLandOfColorTappedForManaEffect(ManaColor.COLORLESS);

            tapLand(ultima, player1Id, effect, player1Id, wastes.getId(), ManaColor.COLORLESS);
            tapLand(ultima, player1Id, effect, player1Id, UUID.randomUUID(), ManaColor.COLORLESS);

            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.COLORLESS)).isEqualTo(1);
        }

        @Test
        @DisplayName("waits for the land's mana color choice, then adds the produced color to the tapping player")
        void waitsForTheLandsManaColorChoice() {
            Permanent manaFlare = createPermanent("Mana Flare");
            Permanent land = new Permanent(createCard("City of Brass", CardType.LAND));
            when(gameQueryService.findPermanentById(gd, land.getId())).thenReturn(land);
            var effect = new AddOneOfEachManaTypeProducedByLandEffect();
            gd.interaction.beginInteraction(new PendingInteraction.ColorChoice(
                    player2Id, null, null, null, List.of("RED"), "Choose mana."));

            assertThat(tapLand(manaFlare, player1Id, effect, player2Id, land.getId())).isTrue();

            StackEntry parked = gd.pendingManaAbilityTriggers.getFirst();
            assertThat(parked.getEffectsToResolve()).containsExactly(effect);
            assertThat(parked.getTriggeringPlayerId()).isEqualTo(player2Id);
            assertThat(parked.getTriggeringPermanentId()).isEqualTo(land.getId());
            assertThat(parked.isAwaitingProducedManaColor()).isTrue();
            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.RED)).isZero();

            parked.setProducedManaColors(Set.of(ManaColor.RED));
            addOneOfEachHandler.resolve(gd, parked, effect);

            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.RED)).isEqualTo(1);
            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.RED)).isZero();
        }

        @Test
        @DisplayName("\"one mana of any color\" doesn't wait for the land's color choice: its own choice is queued")
        void anyColorManaQueuesItsOwnChoiceBehindTheLandsChoice() {
            Permanent behemoth = createPermanent("Regal Behemoth");
            Permanent land = new Permanent(createCard("City of Brass", CardType.LAND));
            var effect = AddOneOfEachManaTypeProducedByLandEffect.ofAnyColor();
            var landChoice = new PendingInteraction.ColorChoice(
                    player1Id, null, null, null, List.of("RED"), "Choose mana.");
            gd.interaction.beginInteraction(landChoice);

            assertThat(tapLand(behemoth, player1Id, effect, player1Id, land.getId())).isTrue();

            assertThat(gd.pendingManaAbilityTriggers).isEmpty();
            assertThat(gd.interaction.activeInteraction()).isSameAs(landChoice);
            var queued = (PendingInteraction.ColorChoice) gd.pendingInteractions.getFirst();
            assertThat(queued.playerId()).isEqualTo(player1Id);
            assertThat(queued.options()).containsExactly("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        }

        @Test
        @DisplayName("a mana-and-damage sequence resolves at once, every step reading the record")
        void resolvesCombinedManaAndDamageAbilityImmediately() {
            Permanent overabundance = createPermanent("Overabundance");
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            when(gameQueryService.findPermanentById(gd, forest.getId())).thenReturn(forest);
            var damage = new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER);
            var effect = SequenceEffect.of(new AddOneOfEachManaTypeProducedByLandEffect(), damage);

            assertThat(tapLand(overabundance, player1Id, effect, player2Id, forest.getId())).isTrue();

            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(gd.stack).isEmpty();
            verify(dealDamageHandler).resolve(eq(gd), org.mockito.ArgumentMatchers.argThat(entry ->
                    player2Id.equals(entry.getTriggeringPlayerId())
                            && forest.getId().equals(entry.getTriggeringPermanentId())), eq(damage));
        }

        @Test
        @DisplayName("a triggered mana ability's judged intervening \"if\" comes off, and its sequence resolves at once")
        void resolvesTappedSourceConditionalSequenceImmediately() {
            Permanent gremlin = createPermanent("Barbflare Gremlin");
            gremlin.tap();
            Permanent forest = createLandPermanent("Forest", ManaColor.GREEN);
            when(gameQueryService.findPermanentById(gd, forest.getId())).thenReturn(forest);
            var sequence = SequenceEffect.of(new AddOneOfEachManaTypeProducedByLandEffect(),
                    new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER).dealtByTriggeringPermanent());
            var ability = new TriggeredAbility(Triggers.playerTapsLandForMana(),
                    TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped()), sequence);
            CardEffect effect = TapTriggers.read(ability, Triggers.playerTapsLandForMana())
                    .getFirst().effectAsJudged();
            assertThat(effect).isEqualTo(sequence);

            assertThat(tapLand(gremlin, player1Id, effect, player2Id, forest.getId())).isTrue();

            assertThat(gd.playerManaPools.get(player2Id).get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("adds nothing when the tapped land is gone and the tap reported no mana")
        void addsNothingWhenLandGone() {
            Permanent wake = createPermanent("Mirari's Wake");
            var effect = new AddOneOfEachManaTypeProducedByLandEffect();

            tapLand(wake, player1Id, effect, player1Id, UUID.randomUUID());

            assertThat(gd.playerManaPools.get(player1Id).getTotalAllMana()).isZero();
        }
    }

    @Nested
    @DisplayName("a permanent tapped for mana")
    class PermanentTapForMana {

        @Test
        @DisplayName("Treasure Nabber's \"that artifact\" is the tapped permanent, recorded with the tapping player")
        void thatPermanentIsTheTappedOne() {
            Permanent nabber = createPermanent("Treasure Nabber");
            Permanent artifact = new Permanent(createCard("Mind Stone", CardType.ARTIFACT));
            var effect = GainControlOfTargetEffect.ofTriggeringPermanent(ControlDuration.UNTIL_END_OF_YOUR_NEXT_TURN);

            assertThat(sut.collect(gd, source(nabber, player1Id, effect), player2Id, artifact.getId(),
                    Set.of(ManaColor.COLORLESS), artifact.getId(), true)).isTrue();

            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getTargetId()).isEqualTo(artifact.getId());
            assertThat(entry.isNonTargeting()).isTrue();
            assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2Id);
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(artifact.getId());
        }

        @Test
        @DisplayName("Kinnan's mana waits for the permanent's open any-color choice, then is that type")
        void waitsForTheOpenChoice() {
            Permanent kinnan = createPermanent("Kinnan, Bonder Prodigy");
            UUID manaRockId = UUID.randomUUID();
            var effect = new AddManaOfTypeProducedByTappedPermanentEffect();

            assertThat(sut.collect(gd, source(kinnan, player1Id, effect), player1Id, manaRockId, null, manaRockId,
                    true)).isTrue();

            StackEntry parked = gd.pendingManaAbilityTriggers.getFirst();
            assertThat(parked.isAwaitingProducedManaColor()).isTrue();
            assertThat(parked.getTriggeringPlayerId()).isEqualTo(player1Id);

            parked.setProducedManaColors(Set.of(ManaColor.BLUE));
            effectHandlerRegistry.getHandler(effect).resolve(gd, parked, effect);

            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.BLUE)).isEqualTo(1);
        }

        @Test
        @DisplayName("a creature tapped for mana adds the additional mana to the player who tapped it (Badgermole Cub)")
        void creatureTapAddsAdditionalMana() {
            Permanent cub = createPermanent("Badgermole Cub");
            var effect = new AddManaWhenTappedForManaEffect(ManaColor.GREEN);

            UUID creatureId = UUID.randomUUID();
            assertThat(sut.collect(gd, source(cub, player1Id, effect), player1Id, creatureId, Set.of(), creatureId,
                    true)).isTrue();

            assertThat(gd.playerManaPools.get(player1Id).get(ManaColor.GREEN)).isEqualTo(1);
            assertThat(gd.stack).isEmpty();
        }
    }
}
