package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Wernog's opponent choices and its controller's resulting investigations. */
@Component
@RequiredArgsConstructor
public class EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffectHandler
        implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var investigateEffect = (EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect) effect;
        UUID controllerId = investigateEffect.controllerId() != null
                ? investigateEffect.controllerId() : entry.getControllerId();
        List<UUID> opponents = investigateEffect.remainingOpponentIds().isEmpty()
                ? new ArrayList<>(EachPlayerMayScryEffectHandler.apnapOpponents(gameData, controllerId))
                : new ArrayList<>(investigateEffect.remainingOpponentIds());
        opponents.removeIf(id -> !gameData.playerIds.contains(id));

        if (opponents.isEmpty()) {
            investigate(gameData, entry, controllerId, investigateEffect.investigatedOpponentCount() + 1);
            return;
        }

        promptNext(gameData, entry.getCard(), new EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect(
                opponents, controllerId, investigateEffect.investigatedOpponentCount()));
    }

    public void promptNext(GameData gameData, Card sourceCard,
                           EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                sourceCard.getName() + " — You may investigate. If you don't, you lose 1 life."));
    }

    public void completeChoice(GameData gameData, PendingMayAbility ability,
                               EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect effect,
                               boolean accepted) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        if (accepted) {
            investigate(gameData, ability.sourceCard(), opponentId, 1, ability.sourcePermanentId(),
                    ability.sourcePermanentSnapshot());
        } else {
            lifeSupport.applyLifeLoss(gameData, opponentId, 1, ability.sourceCard().getName());
        }

        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.removeFirst();
        int investigatedCount = effect.investigatedOpponentCount() + (accepted ? 1 : 0);
        if (remaining.isEmpty()) {
            investigate(gameData, ability.sourceCard(), effect.controllerId(), investigatedCount + 1,
                    ability.sourcePermanentId(), ability.sourcePermanentSnapshot());
        } else {
            promptNext(gameData, ability.sourceCard(),
                    new EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect(
                            remaining, effect.controllerId(), investigatedCount));
        }
    }

    private void investigate(GameData gameData, StackEntry entry, UUID playerId, int amount) {
        investigate(gameData, entry.getCard(), playerId, amount,
                entry.getSourcePermanentId(), entry.getSourcePermanentSnapshot());
    }

    private void investigate(GameData gameData, Card sourceCard, UUID playerId, int amount,
                             UUID sourcePermanentId, Permanent sourcePermanentSnapshot) {
        StackEntry tokenEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                playerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(CreateTokenEffect.ofClueToken(amount))),
                0,
                sourcePermanentId);
        tokenEntry.setSourcePermanentSnapshot(sourcePermanentSnapshot);
        createTokenEffectHandler.resolveForController(
                gameData, tokenEntry, CreateTokenEffect.ofClueToken(amount), playerId);
    }
}
