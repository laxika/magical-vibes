package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerTestFixtures;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
abstract class AbstractDamageHandlerTest {

    @Mock protected GraveyardService graveyardService;
    @Mock protected DamagePreventionService damagePreventionService;
    @Mock protected GameOutcomeService gameOutcomeService;
    @Mock protected GameQueryService gameQueryService;
    @Mock protected PredicateEvaluationService predicateEvaluationService;
    @Mock protected GameBroadcastService gameBroadcastService;
    @Mock protected PermanentRemovalService permanentRemovalService;
    @Mock protected TriggerCollectionService triggerCollectionService;
    @Mock protected LifeSupport lifeSupport;

    @InjectMocks protected DamageSupport damageSupport;

    /** Real evaluator over the mocked collaborators — Fixed/XValue amounts never touch the mocks. */
    protected AmountEvaluationService amountEvaluationService;

    protected GameData gd;
    protected UUID player1Id;
    protected UUID player2Id;

    @BeforeEach
    void setUpDamageHandlerBase() {
        var game = EffectHandlerTestFixtures.newTwoPlayerGameData(true);
        player1Id = game.player1Id();
        player2Id = game.player2Id();
        gd = game.gameData();
        amountEvaluationService = new AmountEvaluationService(predicateEvaluationService, gameQueryService);
        lenient().when(gameQueryService.getEnchantedPlayerDamageMultiplier(eq(gd), any(UUID.class))).thenReturn(1);
        setUpHandler();
    }

    protected abstract void setUpHandler();

    protected static Card createCard(String name) {
        return EffectHandlerTestFixtures.createInstantCard(name);
    }

    protected static Card createCreature(String name, int power, int toughness) {
        return EffectHandlerTestFixtures.createCreatureCard(name, power, toughness);
    }

    protected Permanent addPermanent(UUID playerId, Card card) {
        return EffectHandlerTestFixtures.addPermanent(gd, playerId, card);
    }

    protected StackEntry createEntry(Card card, UUID controllerId, UUID targetId) {
        StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card, controllerId, card.getName(), List.of());
        entry.setTargetId(targetId);
        return entry;
    }

    protected StackEntry createEntryWithXValue(Card card, UUID controllerId, int xValue, UUID targetId) {
        StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(), List.of(), xValue);
        entry.setTargetId(targetId);
        return entry;
    }

    protected StackEntry createMultiTargetEntry(Card card, UUID controllerId, List<UUID> targetIds) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(), List.of(), 0, targetIds);
    }

    protected void stubDamagePreventable() {
        when(gameQueryService.isDamagePreventable(gd)).thenReturn(true);
    }

    protected void stubDamageFromSourceNotPrevented() {
        when(gameQueryService.isDamageFromSourcePrevented(eq(gd), any())).thenReturn(false);
    }

    protected void stubNoDamageMultiplier() {
        when(gameQueryService.applyDamageMultiplier(eq(gd), anyInt(), any(StackEntry.class))).thenAnswer(inv -> inv.getArgument(1));
    }

    protected void stubCreatureDamageCore(Permanent target, int toughness) {
        lenient().when(gameQueryService.findPermanentController(eq(gd), eq(target.getId()))).thenReturn(player2Id);
        when(damagePreventionService.applyCreaturePreventionShield(eq(gd), eq(target), anyInt())).thenAnswer(inv -> inv.getArgument(2));
        when(gameQueryService.getEffectiveToughness(gd, target)).thenReturn(toughness);
    }

    protected void stubCreatureSourceRedirects() {
        when(damagePreventionService.applySourceRedirectShields(eq(gd), any(), any(), anyInt())).thenAnswer(inv -> inv.getArgument(3));
        when(damagePreventionService.applyCreatureRedirectShields(eq(gd), any(), any(), anyInt())).thenAnswer(inv -> inv.getArgument(3));
        when(damagePreventionService.applyTargetSourcePreventionShield(eq(gd), any(), any(), anyInt())).thenAnswer(inv -> inv.getArgument(3));
        when(damagePreventionService.applyChosenSourceNextDamageToAnyTargetShield(eq(gd), any(), anyInt())).thenAnswer(inv -> inv.getArgument(2));
    }

    protected void stubLethalDamage(boolean isLethal) {
        when(gameQueryService.isLethalDamage(anyInt(), anyInt(), anyBoolean())).thenReturn(isLethal);
    }

    protected void stubNoKeywordsOnSource(StackEntry entry) {
        when(gameQueryService.sourceHasKeyword(eq(gd), eq(entry), isNull(), eq(Keyword.INFECT))).thenReturn(false);
        when(gameQueryService.sourceHasKeyword(eq(gd), eq(entry), isNull(), eq(Keyword.DEATHTOUCH))).thenReturn(false);
    }

    protected void stubNoKeywordsOnSourceWithDamageSource(StackEntry entry, Permanent damageSource) {
        when(gameQueryService.sourceHasKeyword(eq(gd), eq(entry), eq(damageSource), eq(Keyword.INFECT))).thenReturn(false);
        when(gameQueryService.sourceHasKeyword(eq(gd), eq(entry), eq(damageSource), eq(Keyword.DEATHTOUCH))).thenReturn(false);
    }

    protected void stubNoInfectOnSource(StackEntry entry) {
        when(gameQueryService.sourceHasKeyword(eq(gd), eq(entry), isNull(), eq(Keyword.INFECT))).thenReturn(false);
    }

    protected void stubPlayerDamageCore(UUID playerId) {
        when(damagePreventionService.isSourceDamagePreventedForPlayer(eq(gd), eq(playerId), any())).thenReturn(false);
        when(damagePreventionService.applySourceRedirectShields(eq(gd), eq(playerId), any(), anyInt())).thenAnswer(inv -> inv.getArgument(3));
        when(damagePreventionService.applyColorDamagePreventionForPlayer(eq(gd), eq(playerId), any())).thenReturn(false);
        when(damagePreventionService.applyOpponentSourceDamageReduction(eq(gd), eq(playerId), any(), anyInt())).thenAnswer(inv -> inv.getArgument(3));
        when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(playerId), anyInt())).thenAnswer(inv -> inv.getArgument(2));
        when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(playerId), anyInt(), anyString())).thenAnswer(inv -> inv.getArgument(2));
        when(gameQueryService.canPlayerLifeChange(gd, playerId)).thenReturn(true);
        when(gameQueryService.shouldDamageBeDealtAsInfect(gd, playerId)).thenReturn(false);
    }
}
