package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BronzeTabletAnteExchangeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link BronzeTabletAnteExchangeEffect} (Bronze Tablet). Exiles both Bronze Tablet and the
 * targeted permanent, then lets the permanent's owner decide: a payable owner is prompted via the
 * may-ability system (the accept/decline branch lives in {@code BronzeTabletAnteExchangeHandler}); an
 * owner who can't pay resolves the ante swap immediately.
 *
 * <p>Ownership changes replace the immutable exiled cards with frozen runtime copies that
 * retain the card identities and the new owners.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BronzeTabletAnteExchangeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BronzeTabletAnteExchangeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (BronzeTabletAnteExchangeEffect) effect;

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            // Target left before resolution — the ability does nothing (and Bronze Tablet stays put).
            return;
        }

        // "That player" — the opponent who owns the targeted permanent — pays or loses ownership.
        UUID targetController = gameQueryService.findPermanentController(gameData, target.getId());
        UUID opponentId = target.getOriginalCard().getOwnerId() != null
                ? target.getOriginalCard().getOwnerId()
                : gameData.stolenCreatures.getOrDefault(target.getId(), targetController);
        String opponentName = gameData.playerIdToName.get(opponentId);

        // Exile the targeted permanent (unconditional — happens before the pay decision).
        permanentRemovalService.removePermanentToExile(gameData, target);
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));

        // Exile Bronze Tablet itself if it's still on the battlefield (ruling: if it isn't, it isn't exiled).
        Card tabletCard = entry.getCard();
        Permanent tabletPermanent = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (tabletPermanent != null) {
            tabletCard = tabletPermanent.getOriginalCard();
            permanentRemovalService.removePermanentToExile(gameData, tabletPermanent);
            gameLogService.append(gameData, GameLog.cardThen(tabletCard, " is exiled."));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        boolean canPay = gameQueryService.canPlayerLoseLife(gameData, opponentId)
                && gameData.getLife(opponentId) >= e.lifeCost();

        if (!canPay) {
            exchangeExiledOwnership(gameData, tabletCard.getId(), opponentId,
                    target.getOriginalCard().getId(), entry.getControllerId());
            // Can't pay — the ante swap happens. Ownership changes aren't modeled, so within one game
            // both cards simply remain exiled.
            gameLogService.append(gameData, GameLog.textCardText(opponentName + " can't pay " + e.lifeCost() + " life — ownership of the exiled cards is exchanged. (", tabletCard, ")"));
            log.info("Game {} - {} can't pay {} life, {} ante swap resolves", gameData.id, opponentName,
                    e.lifeCost(), tabletCard.getName());
            return;
        }

        // Payable — ask the owner. The deciding player rides in the controllerId slot; the Bronze Tablet
        // card is the source so the pay branch can move it from exile to its owner's graveyard.
        String prompt = "Pay " + e.lifeCost() + " life? If you don't, ownership of the exiled cards is "
                + "exchanged. (" + tabletCard.getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                tabletCard, opponentId, List.of(e), prompt, target.getOriginalCard().getId(),
                null, entry.getSourcePermanentId(), null, 0, 0, null, null, null,
                entry.getSourcePermanentSnapshot(), entry.getControllerId()));
    }
    /** Applies the ownership changes to the exiled objects while retaining their identities. */
    public static void exchangeExiledOwnership(GameData gameData, UUID tabletId, UUID tabletNewOwner,
                                               UUID otherCardId, UUID otherNewOwner) {
        replaceExiledOwner(gameData, tabletId, tabletNewOwner);
        replaceExiledOwner(gameData, otherCardId, otherNewOwner);
    }

    private static void replaceExiledOwner(GameData gameData, UUID cardId, UUID ownerId) {
        if (cardId == null || ownerId == null) return;
        for (int i = 0; i < gameData.exiledCards.size(); i++) {
            ExiledCardEntry exiled = gameData.exiledCards.get(i);
            if (!exiled.card().getId().equals(cardId)) continue;
            Card copy = exiled.card().createRuntimeCopy();
            copy.setOwnerId(ownerId);
            copy.freeze();
            gameData.exiledCards.set(i, new ExiledCardEntry(copy, ownerId, exiled.sourcePermanentId(),
                    exiled.faceDown(), exiled.exilerId(), exiled.exiledTurnNumber(), exiled.controllerTurnsTakenAtExile(), exiled.abilityLink()));
            return;
        }
    }
}
