package com.github.laxika.magicalvibes.service.battle;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BattleDefeatedExileAndCastTransformedEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Siege battle defeat: when the last defense counter is removed, queue the intrinsic triggered
 * ability that exiles the battle and casts it transformed.
 */
@Slf4j
@Component
public class BattleDefeatSupport {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;

    public BattleDefeatSupport(GameQueryService gameQueryService,
                               GameBroadcastService gameBroadcastService,
                               PermanentRemovalService permanentRemovalService,
                               @Lazy TriggerCollectionService triggerCollectionService) {
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.permanentRemovalService = permanentRemovalService;
        this.triggerCollectionService = triggerCollectionService;
    }

    /**
     * Call after defense counters change. If the battle has ≤0 defense and is a Siege (or has a
     * back face), queue the defeat trigger unless one is already on the stack.
     */
    public void checkAfterDefenseRemoved(GameData gameData, Permanent battle) {
        if (battle == null || battle.getCounterCount(CounterType.DEFENSE) > 0) {
            return;
        }
        if (!gameQueryService.isBattle(gameData, battle)) {
            return;
        }
        Card card = battle.getOriginalCard() != null ? battle.getOriginalCard() : battle.getCard();
        boolean isSiege = card.getSubtypes().contains(CardSubtype.SIEGE)
                || battle.getCard().getSubtypes().contains(CardSubtype.SIEGE);
        if (!isSiege && card.getBackFaceCard() == null) {
            return;
        }
        if (hasDefeatTriggerOnStack(gameData, battle.getId())) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, battle.getId());
        if (controllerId == null) {
            return;
        }

        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                controllerId,
                card.getName() + " — When this battle is defeated, exile it, then cast it transformed.",
                List.of(new BattleDefeatedExileAndCastTransformedEffect()),
                null,
                battle.getId());
        gameData.stack.add(trigger);
        gameData.priorityPassedBy.clear();

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(card, " is defeated."));
        log.info("Game {} - Battle {} defeated; defeat trigger queued", gameData.id, card.getName());
    }

    public boolean hasDefeatTriggerOnStack(GameData gameData, UUID battlePermanentId) {
        return gameData.stack.stream().anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && battlePermanentId.equals(e.getSourcePermanentId())
                        && e.getEffectsToResolve() != null
                        && e.getEffectsToResolve().stream()
                        .anyMatch(BattleDefeatedExileAndCastTransformedEffect.class::isInstance));
    }

    /**
     * Exile the battle and put its transformed spell on the stack without paying mana.
     */
    public void resolveDefeat(GameData gameData, StackEntry entry) {
        UUID sourceId = entry.getSourcePermanentId();
        Permanent battle = sourceId != null ? gameQueryService.findPermanentById(gameData, sourceId) : null;
        if (battle == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(entry.getDescription() + " fizzles (battle gone)."));
            return;
        }

        Card frontCard = battle.getOriginalCard() != null ? battle.getOriginalCard() : battle.getCard();
        Card backFace = frontCard.getBackFaceCard();
        UUID controllerId = entry.getControllerId();
        UUID ownerId = frontCard.getOwnerId() != null ? frontCard.getOwnerId() : controllerId;

        permanentRemovalService.removePermanentToExile(gameData, battle);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(frontCard, " is exiled (defeated)."));

        ExiledCardEntry exiled = gameData.findExiledCard(frontCard.getId());
        if (exiled == null) {
            log.warn("Game {} - Defeated battle {} not found in exile after removal", gameData.id, frontCard.getName());
            return;
        }

        gameData.removeFromExile(frontCard.getId());

        StackEntryType spellType = mapBackFaceSpellType(backFace != null ? backFace : frontCard);
        StackEntry spell = new StackEntry(
                spellType,
                frontCard,
                controllerId,
                backFace != null ? backFace.getName() : frontCard.getName(),
                List.of());
        spell.setCastTransformed(true);
        spell.setOwnerIdOverride(ownerId.equals(controllerId) ? null : ownerId);
        gameData.stack.add(spell);
        gameData.recordSpellCast(controllerId, frontCard);
        gameData.priorityPassedBy.clear();

        String playerName = gameData.playerIdToName.get(controllerId);
        Card announced = backFace != null ? backFace : frontCard;
        gameBroadcastService.logAndBroadcast(gameData, GameLog.playerPlays(playerName, announced,
                " transformed without paying its mana cost."));
        log.info("Game {} - {} casts defeated battle {} transformed", gameData.id, playerName, frontCard.getName());

        triggerCollectionService.checkSpellCastTriggers(gameData, frontCard, controllerId, false);
    }

    private static StackEntryType mapBackFaceSpellType(Card card) {
        if (card.hasType(CardType.CREATURE)) {
            return StackEntryType.CREATURE_SPELL;
        }
        if (card.hasType(CardType.ARTIFACT)) {
            return StackEntryType.ARTIFACT_SPELL;
        }
        if (card.hasType(CardType.PLANESWALKER)) {
            return StackEntryType.PLANESWALKER_SPELL;
        }
        if (card.hasType(CardType.BATTLE)) {
            return StackEntryType.BATTLE_SPELL;
        }
        return StackEntryType.ENCHANTMENT_SPELL;
    }
}
