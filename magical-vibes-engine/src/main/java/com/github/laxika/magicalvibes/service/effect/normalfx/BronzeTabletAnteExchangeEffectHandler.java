package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BronzeTabletAnteExchangeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
 * <p>The ante "that player owns this card and you own the other exiled card" is resolved as the
 * single-game observable zone movements — see {@link BronzeTabletAnteExchangeEffect} — never a
 * runtime {@code ownerId} change.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BronzeTabletAnteExchangeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
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
        UUID opponentId = gameData.stolenCreatures.getOrDefault(target.getId(), targetController);
        String opponentName = gameData.playerIdToName.get(opponentId);

        // Exile the targeted permanent (unconditional — happens before the pay decision).
        permanentRemovalService.removePermanentToExile(gameData, target);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(), " is exiled."));

        // Exile Bronze Tablet itself if it's still on the battlefield (ruling: if it isn't, it isn't exiled).
        Card tabletCard = entry.getCard();
        Permanent tabletPermanent = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (tabletPermanent != null) {
            tabletCard = tabletPermanent.getOriginalCard();
            permanentRemovalService.removePermanentToExile(gameData, tabletPermanent);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(tabletCard, " is exiled."));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, opponentId)
                && gameData.getLife(opponentId) >= e.lifeCost();

        if (!canPay) {
            // Can't pay — the ante swap happens. Ownership changes aren't modeled, so within one game
            // both cards simply remain exiled.
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(opponentName + " can't pay " + e.lifeCost() + " life — ownership of the exiled cards is exchanged. (", tabletCard, ")"));
            log.info("Game {} - {} can't pay {} life, {} ante swap resolves", gameData.id, opponentName,
                    e.lifeCost(), tabletCard.getName());
            return;
        }

        // Payable — ask the owner. The deciding player rides in the controllerId slot; the Bronze Tablet
        // card is the source so the pay branch can move it from exile to its owner's graveyard.
        String prompt = "Pay " + e.lifeCost() + " life? If you don't, ownership of the exiled cards is "
                + "exchanged. (" + tabletCard.getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(tabletCard, opponentId, List.of(e), prompt));
    }
}
