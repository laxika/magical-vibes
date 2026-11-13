package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.AllowCastTargetCardFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.MayEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.normalfx.BrilliantUltimatumSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.BendOrBreakEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.FightOrFlightSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.MakeAnExampleEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.RagingRiverEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.StandOrFallSupport;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * The answer to the "you may" of a resolving entry (CR 603.5) chooses nothing: the entry's targets were chosen as it
 * was put on the stack (CR 603.3d), so an accepted effect resumes the resolution with the targets the entry has, or
 * with none.
 *
 * <p>An accepted option that has no handler of its own is carried out at once, in the resolution that asked, by an
 * entry that keeps what the question recorded; nothing is put on the stack for it.</p>
 */
class MayAbilityHandlerServiceTest {

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    private GameQueryService gameQueryService;
    private MayEffectHandlerRegistry mayEffectHandlerRegistry;
    private EffectResolutionService effectResolutionService;
    private MayAbilityHandlerService svc;

    private GameData gd;
    private Player player1;

    @BeforeEach
    void setUp() {
        gameQueryService = mock(GameQueryService.class);
        mayEffectHandlerRegistry = mock(MayEffectHandlerRegistry.class);
        effectResolutionService = mock(EffectResolutionService.class);

        svc = new MayAbilityHandlerService(
                mock(InputCompletionService.class),
                mock(MayCastHandlerService.class),
                mock(MayCopyHandlerService.class),
                mock(MayMiscHandlerService.class),
                gameQueryService,
                mock(GameLogService.class),
                effectResolutionService,
                mock(DestructionSupport.class),
                mock(BendOrBreakEffectHandler.class),
                mock(FightOrFlightSupport.class),
                mock(StandOrFallSupport.class),
                mock(MakeAnExampleEffectHandler.class),
                mock(GraveyardReturnSupport.class),
                mock(BrilliantUltimatumSupport.class),
                mock(MayAbilityTapCostService.class),
                mayEffectHandlerRegistry,
                mock(TriggerCollectionService.class),
                mock(RagingRiverEffectHandler.class));

        player1 = new Player(PLAYER1_ID, "Alice");

        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Alice");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Alice");
        gd.playerIdToName.put(PLAYER2_ID, "Bob");
        gd.playerBattlefields.put(PLAYER1_ID, new ArrayList<>());
        gd.playerBattlefields.put(PLAYER2_ID, new ArrayList<>());
    }

    @Test
    @DisplayName("An accepted effect with a permanent target and none on its entry asks for no target as it resolves")
    void acceptedPermanentTargetedMayChoosesNoTargetOnResolution() {
        Permanent bear = permanent("Grizzly Bears", CardType.CREATURE);
        gd.playerBattlefields.get(PLAYER1_ID).add(bear);

        StackEntry pendingEntry = acceptMayAbility(specEffect(TargetPredicates.creature()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.resolvedMayAccepted).isTrue();
        assertThat(pendingEntry.getTargetId()).isNull();
        assertThat(pendingEntry.getTargetIds()).isEmpty();
        verify(effectResolutionService).resolveEffectsFrom(gd, pendingEntry, 0);
    }

    @Test
    @DisplayName("An accepted effect with a player target and none on its entry asks for no target as it resolves")
    void acceptedPlayerTargetedMayChoosesNoTargetOnResolution() {
        StackEntry pendingEntry = acceptMayAbility(specEffect(TargetPredicates.player()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.resolvedMayAccepted).isTrue();
        assertThat(pendingEntry.getTargetId()).isNull();
        verify(effectResolutionService).resolveEffectsFrom(gd, pendingEntry, 0);
    }

    @Test
    @DisplayName("A preselected graveyard target is not selected again after accepting a sequence may")
    void preselectedGraveyardTargetIsRetainedAfterMayAcceptance() {
        Card sourceCard = new Card();
        sourceCard.setName("Test Source");
        Card targetCard = new Card();
        targetCard.setName("Target Card");
        gd.playerGraveyards.put(PLAYER1_ID, new ArrayList<>(List.of(targetCard)));

        CardEffect sequence = SequenceEffect.of(
                new ExileSourceCardFromGraveyardEffect(),
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY)
                        .targetGraveyard(true)
                        .build());
        StackEntry pendingEntry = new StackEntry(
                com.github.laxika.magicalvibes.model.StackEntryType.TRIGGERED_ABILITY,
                sourceCard, PLAYER1_ID, "Test Source's ability", List.of(new MayEffect(sequence, "Exile it?")),
                List.of(targetCard.getId()));
        gd.pendingEffectResolutionEntry = pendingEntry;
        gd.pendingEffectResolutionIndex = 0;
        gd.resolvingMayEffectFromStack = true;
        gd.pendingMayAbilities.add(new PendingMayAbility(
                sourceCard, PLAYER1_ID, List.of(sequence), "Exile it?"));
        gd.interaction.beginInteraction(new PendingInteraction.MayAbilityChoice(PLAYER1_ID, "Exile it?", null));

        svc.handleMayAbilityChosen(gd, player1, true);

        verify(effectResolutionService).resolveEffectsFrom(gd, pendingEntry, 0);
        verify(mayEffectHandlerRegistry, org.mockito.Mockito.never()).getHandler(any());
    }

    @Test
    @DisplayName("An accepted effect with a graveyard target and none on its entry picks no card as it resolves")
    void acceptedGraveyardTargetedMayChoosesNoCardOnResolution() {
        Card enchantment = new Card();
        enchantment.setName("Enchantment");
        enchantment.setType(CardType.ENCHANTMENT);
        gd.playerGraveyards.put(PLAYER1_ID, new ArrayList<>(List.of(enchantment)));

        StackEntry pendingEntry = acceptMayAbility(new AllowCastTargetCardFromGraveyardThisTurnEffect(
                new CardTypePredicate(CardType.ENCHANTMENT),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                false));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(pendingEntry.getTargetId()).isNull();
        assertThat(pendingEntry.getTargetCardIds()).isEmpty();
        verify(effectResolutionService).resolveEffectsFrom(gd, pendingEntry, 0);
    }

    @Test
    @DisplayName("An accepted option resolves at once with the triggering card the question kept")
    void acceptedSpellCastMayAbilityPreservesTriggeringCardId() {
        Card sourceCard = new Card();
        sourceCard.setName("Test Source");
        sourceCard.setType(CardType.ENCHANTMENT);
        UUID sourcePermanentId = UUID.randomUUID();
        UUID triggeringCardId = UUID.randomUUID();
        CardEffect effect = new CardEffect() { };

        gd.pendingMayAbilities.add(new PendingMayAbility(sourceCard, PLAYER1_ID, List.of(effect),
                "Test Source — effect?", null, null, sourcePermanentId, null, 0, 0, null, null, null, null, null,
                triggeringCardId, 0, null, null, null));
        gd.interaction.beginInteraction(new PendingInteraction.MayAbilityChoice(
                PLAYER1_ID, "Test Source — effect?", null));

        svc.handleMayAbilityChosen(gd, player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(acceptedEntry().getTriggeringCardId()).isEqualTo(triggeringCardId);
    }

    @Test
    @DisplayName("An accepted option a granted ability asked resolves at once with its granting permanent")
    void acceptedGrantedMayAbilityKeepsGrantingPermanent() {
        Card sourceCard = new Card();
        sourceCard.setName("Test Source");
        sourceCard.setType(CardType.CREATURE);
        UUID grantingPermanentId = UUID.randomUUID();
        CardEffect effect = new CardEffect() { };

        gd.pendingMayAbilities.add(new PendingMayAbility(sourceCard, PLAYER1_ID, List.of(effect),
                "Test Source — effect?", null, null, UUID.randomUUID()).withGrantingPermanentId(grantingPermanentId));
        gd.interaction.beginInteraction(new PendingInteraction.MayAbilityChoice(
                PLAYER1_ID, "Test Source — effect?", null));

        svc.handleMayAbilityChosen(gd, player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(acceptedEntry().getGrantingPermanentId()).isEqualTo(grantingPermanentId);
    }

    @Test
    @DisplayName("An accepted option resolves at once with the triggering spell of the ability that asked it")
    void acceptedMayAbilityKeepsTriggeringSpell() {
        Card sourceCard = new Card();
        sourceCard.setName("Test Source");
        sourceCard.setType(CardType.CREATURE);
        Card spellCard = new Card();
        spellCard.setName("Opponent Spell");
        spellCard.setType(CardType.INSTANT);
        CardEffect effect = new CardEffect() { };

        gd.pendingMayAbilities.add(new PendingMayAbility(sourceCard, PLAYER1_ID, List.of(effect),
                "Test Source — effect?", null, null, UUID.randomUUID())
                .withTriggeringSpell(new StackEntry(spellCard, PLAYER2_ID)));
        gd.interaction.beginInteraction(new PendingInteraction.MayAbilityChoice(
                PLAYER1_ID, "Test Source — effect?", null));

        svc.handleMayAbilityChosen(gd, player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(acceptedEntry().getTriggeringSpellSnapshot().getCard()).isSameAs(spellCard);
    }

    @Test
    @DisplayName("An accepted option about a player named beforehand resolves at once for that player")
    void acceptedPreTargetedMayAbilityKeepsTriggeringCard() {
        Card sourceCard = new Card();
        sourceCard.setName("Cloudhoof Kirin");
        sourceCard.setType(CardType.CREATURE);
        UUID spellCardId = UUID.randomUUID();
        CardEffect mill = new MillEffect(new TargetSpellManaValue(), MillRecipient.TARGET_PLAYER);

        gd.pendingMayAbilities.add(new PendingMayAbility(sourceCard, PLAYER1_ID, List.of(mill),
                "Cloudhoof Kirin — mill?", PLAYER2_ID, null, UUID.randomUUID())
                .withTriggeringCard(spellCardId, -1));
        gd.interaction.beginInteraction(new PendingInteraction.MayAbilityChoice(
                PLAYER1_ID, "Cloudhoof Kirin — mill?", null));

        svc.handleMayAbilityChosen(gd, player1, true);

        assertThat(gd.stack).isEmpty();
        StackEntry accepted = acceptedEntry();
        assertThat(accepted.getTargetId()).isEqualTo(PLAYER2_ID);
        assertThat(accepted.isNonTargeting()).isTrue();
        assertThat(accepted.getTriggeringCardId()).isEqualTo(spellCardId);
    }

    @Test
    @DisplayName("An option that needs a target the question did not name is refused: targets are chosen on the stack")
    void acceptedOptionWithoutItsTargetIsRefused() {
        Card sourceCard = new Card();
        sourceCard.setName("Test Source");
        sourceCard.setType(CardType.CREATURE);
        gd.pendingMayAbilities.add(new PendingMayAbility(sourceCard, PLAYER1_ID,
                List.of(specEffect(TargetPredicates.creature())), "Test Source — effect?"));
        gd.interaction.beginInteraction(new PendingInteraction.MayAbilityChoice(
                PLAYER1_ID, "Test Source — effect?", null));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> svc.handleMayAbilityChosen(gd, player1, true))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying a \"you may tap\" cost uses up the ability's \"do this only once each turn\"")
    void paidTapCostMarksTheOnceEachTurnAction() {
        Card sourceCard = new Card();
        sourceCard.setName("Test Source");
        sourceCard.setType(CardType.CREATURE);
        UUID sourcePermanentId = UUID.randomUUID();
        StackEntry resolving = new StackEntry(StackEntryType.TRIGGERED_ABILITY, sourceCard, PLAYER1_ID,
                "Test Source's ability", new ArrayList<>(List.of(new CardEffect() { })), null, sourcePermanentId);
        resolving.setOnceEachTurnActionKey("once");
        gd.pendingEffectResolutionEntry = resolving;
        PendingMayAbility ability = new PendingMayAbility(sourceCard, PLAYER1_ID, List.of(new CardEffect() { }),
                "Test Source — tap?", null, null, sourcePermanentId);

        svc.continueAfterTapCost(gd, player1, ability);

        // CR 603.2h: once its controller took the action this turn, the ability doesn't trigger again.
        assertThat(TriggeredAbilityStackService.actionTaken(gd, sourcePermanentId, "once")).isTrue();
    }

    @Test
    @DisplayName("A declined option does nothing")
    void declinedOptionResolvesNothing() {
        Card sourceCard = new Card();
        sourceCard.setName("Test Source");
        sourceCard.setType(CardType.CREATURE);
        gd.pendingMayAbilities.add(new PendingMayAbility(sourceCard, PLAYER1_ID, List.of(new CardEffect() { }),
                "Test Source — effect?"));
        gd.interaction.beginInteraction(new PendingInteraction.MayAbilityChoice(
                PLAYER1_ID, "Test Source — effect?", null));

        svc.handleMayAbilityChosen(gd, player1, false);

        assertThat(gd.stack).isEmpty();
        verify(effectResolutionService, org.mockito.Mockito.never()).resolveNestedInParkedResolution(any(), any());
    }

    /**
     * Accepts the "you may" of an entry on the stack whose effect was given no target as it was put there.
     *
     * @return the resolving entry
     */
    private StackEntry acceptMayAbility(CardEffect effect) {
        Card sourceCard = new Card();
        sourceCard.setName("Test Source");
        sourceCard.setType(CardType.ENCHANTMENT);
        StackEntry pendingEntry = new StackEntry(
                com.github.laxika.magicalvibes.model.StackEntryType.TRIGGERED_ABILITY,
                sourceCard, PLAYER1_ID, "Test Source's ability", List.of(new MayEffect(effect, "Test may ability")));
        gd.pendingEffectResolutionEntry = pendingEntry;
        gd.pendingEffectResolutionIndex = 0;
        gd.resolvingMayEffectFromStack = true;
        PendingMayAbility ability =
                new PendingMayAbility(sourceCard, PLAYER1_ID, List.of(effect), "Test may ability");
        gd.pendingMayAbilities.add(ability);
        gd.interaction.beginInteraction(
                new PendingInteraction.MayAbilityChoice(PLAYER1_ID, "Test may ability", null));

        svc.handleMayAbilityChosen(gd, player1, true);
        return pendingEntry;
    }

    private StackEntry acceptedEntry() {
        ArgumentCaptor<StackEntry> captor = ArgumentCaptor.forClass(StackEntry.class);
        verify(effectResolutionService).resolveNestedInParkedResolution(eq(gd), captor.capture());
        return captor.getValue();
    }

    private static CardEffect specEffect(TargetPredicate declaredTarget) {
        return new CardEffect() {
            @Override
            public TargetSpec targetSpec() {
                return TargetSpec.benign(declaredTarget);
            }
        };
    }

    private static Permanent permanent(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return new Permanent(card);
    }
}
