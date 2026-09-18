package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.effect.LandPlayPermissionService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/** Completes Word of Command's selected-card play using the targeted player's resources. */
@Slf4j
@Service
@RequiredArgsConstructor
public class WordOfCommandCastSupport {

    @Lazy private final MayCastHandlerService mayCastHandlerService;
    private final SpellCastingService spellCastingService;
    @Lazy private final PlayerInputService playerInputService;
    @Lazy private final BattlefieldEntryService battlefieldEntryService;
    private final LandPlayPermissionService landPlayPermissionService;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;
    @Lazy private final InputCompletionService inputCompletionService;

    public void playSelectedCard(GameData gameData, UUID chooserId, UUID targetPlayerId,
                                 Card card, int originalHandIndex) {
        if (card.hasType(CardType.LAND)) {
            playLand(gameData, chooserId, targetPlayerId, card, originalHandIndex);
            return;
        }

        if (card.isCastOnlyFromGraveyard()) {
            returnToHand(gameData, targetPlayerId, card, originalHandIndex);
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        StackEntryType spellType = spellType(card);
        boolean permanentSpell = card.hasType(CardType.CREATURE)
                || card.hasType(CardType.ARTIFACT)
                || card.hasType(CardType.ENCHANTMENT)
                || card.hasType(CardType.PLANESWALKER)
                || card.hasType(CardType.BATTLE);
        List<CardEffect> spellEffects = permanentSpell
                ? List.of() : new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        int xValue = card.getParsedManaCost() != null && card.getParsedManaCost().hasX()
                ? card.getParsedManaCost().calculateMaxX(gameData.playerManaPools.get(targetPlayerId)) : 0;

        boolean targeted = EffectResolution.needsTarget(card) || EffectResolution.needsSpellTarget(card);
        List<UUID> validTargets = List.of();
        if (targeted) {
            validTargets = mayCastHandlerService.buildValidSpellTargets(
                    gameData, card, spellEffects, targetPlayerId, xValue, false);
            if (validTargets.isEmpty()) {
                returnToHand(gameData, targetPlayerId, card, originalHandIndex);
                inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
                return;
            }
        }

        try {
            if (card.getParsedManaCost() != null) {
                spellCastingService.paySpellManaCost(gameData, targetPlayerId, card, xValue, List.of());
            }
        } catch (IllegalStateException ex) {
            log.info("Game {} - {} cannot pay to play {} with Word of Command",
                    gameData.id, gameData.playerIdToName.get(targetPlayerId), card.getName());
            returnToHand(gameData, targetPlayerId, card, originalHandIndex);
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        if (targeted) {
            beginResolutionControl(gameData, chooserId, targetPlayerId, card);
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.HandCastSpellTarget(
                    card, targetPlayerId, spellEffects, spellType, xValue, false, false));
            playerInputService.beginPermanentChoice(gameData, chooserId, validTargets,
                    "Choose a target for " + card.getName() + ".");
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(chooserId) + " chooses and casts ", card,
                    " from " + gameData.playerIdToName.get(targetPlayerId) + "'s hand."));
            return;
        }

        StackEntry entry = new StackEntry(
                spellType, card, targetPlayerId, card.getName(), spellEffects, xValue, (UUID) null, null);
        entry.setSourceZone(Zone.HAND);
        if (card.getOwnerId() != null && !card.getOwnerId().equals(targetPlayerId)) {
            entry.setOwnerIdOverride(card.getOwnerId());
        }
        beginResolutionControl(gameData, chooserId, targetPlayerId, card);
        gameData.stack.add(entry);
        gameData.recordSpellCast(targetPlayerId, card);
        gameData.priorityPassedBy.clear();
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(chooserId) + " chooses and casts ", card,
                " from " + gameData.playerIdToName.get(targetPlayerId) + "'s hand."));
        triggerCollectionService.checkSpellCastTriggers(gameData, card, targetPlayerId, Zone.HAND);
        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void playLand(GameData gameData, UUID chooserId, UUID targetPlayerId,
                          Card card, int originalHandIndex) {
        int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(targetPlayerId, 0);
        if (gameData.playersCantPlayLandsThisTurn.contains(targetPlayerId)
                || landsPlayed >= landPlayPermissionService.getMaxLandsThisTurn(gameData, targetPlayerId)) {
            returnToHand(gameData, targetPlayerId, card, originalHandIndex);
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        Permanent permanent = new Permanent(card);
        battlefieldEntryService.putLandOntoBattlefield(gameData, targetPlayerId, permanent, Zone.HAND);
        gameData.landsPlayedThisTurn.merge(targetPlayerId, 1, Integer::sum);
        gameLogService.append(gameData, GameLog.playerPlays(
                gameData.playerIdToName.get(chooserId), card, " under the targeted player's control."));
        if (!gameData.interaction.isAwaitingInput()) {
            battlefieldEntryService.processLandETBEffects(gameData, targetPlayerId, card);
            if (!gameData.interaction.isAwaitingInput()) {
                triggerCollectionService.checkControllerPlaysLandTriggers(gameData, targetPlayerId, card, Zone.HAND);
            }
        }
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }

    private void returnToHand(GameData gameData, UUID playerId, Card card, int originalHandIndex) {
        List<Card> hand = gameData.playerHands.computeIfAbsent(playerId, ignored -> new ArrayList<>());
        hand.add(Math.min(originalHandIndex, hand.size()), card);
    }

    private void beginResolutionControl(GameData gameData, UUID chooserId, UUID targetPlayerId, Card card) {
        gameData.mindControlledPlayerId = targetPlayerId;
        gameData.mindControllerPlayerId = chooserId;
        gameData.mindControlUntilEndOfCombat = false;
        gameData.mindControlUntilStackCardId = card.getId();
    }

    private StackEntryType spellType(Card card) {
        return switch (card.getType()) {
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            case BATTLE -> StackEntryType.BATTLE_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            default -> throw new IllegalStateException("Unsupported card type: " + card.getType());
        };
    }
}
