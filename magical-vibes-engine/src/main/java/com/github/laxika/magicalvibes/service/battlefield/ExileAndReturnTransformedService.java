package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * "Exile this permanent, then return it to the battlefield transformed under its owner's control."
 * The returned permanent is a brand-new object built from the original card and flipped to its back
 * face — Kytheon, Hero of Akros (at end of combat) and Jace, Vryn's Prodigy (immediately, on
 * resolution) both land here, so the exile-and-return step lives in one place rather than being
 * re-derived per timing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExileAndReturnTransformedService {

    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final SagaChapterService sagaChapterService;

    /**
     * Exiles the given permanent and immediately returns it transformed. No-op when the permanent
     * has already left the battlefield or its card has no back face.
     *
     * @return {@code true} when the permanent was actually exiled — the "if you do" condition
     *         callers such as Liliana, Heretical Healer hang their rider on. A replacement effect
     *         may keep the physical card in exile instead of allowing it to return.
     */
    public boolean exileAndReturnTransformed(GameData gameData, UUID permanentId) {
        return exileAndReturn(gameData, permanentId, true);
    }

    public boolean exileAndReturnFront(GameData gameData, UUID permanentId) {
        return exileAndReturn(gameData, permanentId, false);
    }

    private boolean exileAndReturn(GameData gameData, UUID permanentId, boolean transformed) {
        Permanent perm = null;
        UUID controllerId = null;
        for (Map.Entry<UUID, List<Permanent>> entry : gameData.playerBattlefields.entrySet()) {
            for (Permanent p : entry.getValue()) {
                if (p.getId().equals(permanentId)) {
                    perm = p;
                    controllerId = entry.getKey();
                    break;
                }
            }
            if (perm != null) break;
        }
        if (perm == null) return false;

        Card originalCard = perm.getOriginalCard();
        Card transformedFace = originalCard.getBackFaceCard();
        if (transformedFace == null) return false;
        Card returnedCard = transformed ? originalCard.getBackFaceCard() : originalCard;
        if (returnedCard == null) return false;
        UUID returnControllerId = originalCard.getOwnerId() != null
                ? originalCard.getOwnerId() : controllerId;

        boolean returningTransformed = !perm.isTransformed();
        Card returningCard = returningTransformed ? transformedFace : originalCard;

        UUID ownerId = originalCard.getOwnerId() != null ? originalCard.getOwnerId() : controllerId;
        permanentRemovalService.removePermanentToExile(gameData, perm);
        // Removed from exile immediately — it returns right away on the opposite face.
        gameData.removeFromExile(originalCard.getId());

        Permanent newPerm = new Permanent(originalCard);
        newPerm.setCard(returningCard);
        newPerm.setTransformed(returningTransformed);
        newPerm.setSummoningSick(false);
        newPerm.setEnteredFromExile(true);
        // A back face can be a planeswalker (Kytheon, Hero of Akros; Jace, Vryn's Prodigy): it
        // enters with its starting loyalty, otherwise the state-based check kills it immediately.
        if (returningCard.hasType(CardType.PLANESWALKER) && returningCard.getLoyalty() != null) {
            int loyalty = gameQueryService.replaceCounters(gameData, newPerm, ownerId,
                    CounterType.LOYALTY, returningCard.getLoyalty(), ownerId);
            newPerm.setCounterCount(CounterType.LOYALTY, loyalty);
        }

        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, newPerm);
        if (gameQueryService.findPermanentById(gameData, newPerm.getId()) == null) {
            return true;
        }

        if (gameQueryService.isCreature(gameData, newPerm)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, ownerId, returningCard, null, false);
        }

        if (returningCard.isSaga()) {
            sagaChapterService.initializeSaga(gameData, newPerm, returningCard, ownerId);
        }

        gameLogService.append(gameData, GameLog.cardTextCard(originalCard,
                " is exiled and returns transformed as ", returningCard, "."));
        log.info("Game {} - {} exiled and returned transformed as {}",
                gameData.id, originalCard.getName(), returningCard.getName());
        return true;
    }
    /**
     * Exiles a graveyard-cast spell and immediately returns its physical card transformed under
     * its owner's control.
     *
     * @return {@code true} when the spell had a back face and its physical card was exiled. A
     *         replacement effect may prevent it from entering transformed.
     */
    public boolean exileSpellAndReturnTransformed(GameData gameData, StackEntry entry) {
        if (entry.getSourceZone() != Zone.GRAVEYARD) return false;

        Card originalCard = entry.getPhysicalCard();
        Card transformedFace = originalCard.getBackFaceCard();
        if (transformedFace == null) return false;

        UUID ownerId = entry.getOwnerId();
        gameData.addToExile(ownerId, originalCard);
        gameData.removeFromExile(originalCard.getId());

        Permanent newPerm = new Permanent(originalCard);
        newPerm.setCard(transformedFace);
        newPerm.setTransformed(true);
        newPerm.setSummoningSick(false);
        newPerm.setCastFromZone(Zone.GRAVEYARD);
        newPerm.setEnteredFromGraveyardOwnerId(ownerId);

        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, newPerm);
        if (gameQueryService.findPermanentById(gameData, newPerm.getId()) == null) {
            return true;
        }

        if (gameQueryService.isCreature(gameData, newPerm)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, ownerId, transformedFace, null, false);
        }

        if (transformedFace.isSaga()) {
            sagaChapterService.initializeSaga(gameData, newPerm, transformedFace, ownerId);
        }

        gameLogService.append(gameData, GameLog.cardTextCard(originalCard,
                " is exiled and returns transformed as ", transformedFace,
                " under its owner's control."));
        log.info("Game {} - {} exiled and returned transformed as {}",
                gameData.id, originalCard.getName(), transformedFace.getName());
        return true;
    }

    /** Returns a crafted source card from exile to its owner's battlefield transformed. */
    public boolean returnTransformedFromExile(GameData gameData, UUID cardId, UUID oldSourcePermanentId) {
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (exiled == null) {
            return false;
        }

        Card originalCard = exiled.card();
        Card backFace = originalCard.getBackFaceCard();
        if (backFace == null) {
            return false;
        }

        UUID ownerId = exiled.ownerId();
        gameData.removeFromExile(cardId);

        Permanent newPerm = new Permanent(originalCard);
        newPerm.setCard(backFace);
        newPerm.setTransformed(true);
        newPerm.setSummoningSick(false);
        gameData.transferCardsExiledByPermanent(oldSourcePermanentId, newPerm.getId());
        if (backFace.hasType(CardType.PLANESWALKER) && backFace.getLoyalty() != null) {
            int loyalty = gameQueryService.replaceCounters(gameData, newPerm, ownerId,
                    CounterType.LOYALTY, backFace.getLoyalty(), ownerId);
            newPerm.setCounterCount(CounterType.LOYALTY, loyalty);
        }

        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, newPerm);
        if (gameQueryService.findPermanentById(gameData, newPerm.getId()) == null) {
            return true;
        }
        battlefieldEntryService.handleCreatureEnteredBattlefield(
                gameData, ownerId, backFace, null, false);
        initializeReturnedSaga(gameData, ownerId, newPerm);
        gameLogService.append(gameData, GameLog.cardTextCard(originalCard,
                " returns transformed from exile as ", backFace, "."));
        log.info("Game {} - {} returns transformed from exile as {}",
                gameData.id, originalCard.getName(), backFace.getName());
        return true;
    }

    private void initializeReturnedSaga(GameData gameData, UUID controllerId, Permanent saga) {
        Card card = saga.getCard();
        if (!card.isSaga()) {
            return;
        }

        int loreCounters = gameQueryService.replaceCounters(
                gameData, saga, CounterType.LORE, 1, controllerId);
        saga.setCounterCount(CounterType.LORE, loreCounters);
        List<CardEffect> chapterEffects = card.getEffects(EffectSlot.SAGA_CHAPTER_I);
        if (!chapterEffects.isEmpty()) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s chapter I ability",
                    chapterEffects,
                    null,
                    saga.getId()));
        }
    }
}
