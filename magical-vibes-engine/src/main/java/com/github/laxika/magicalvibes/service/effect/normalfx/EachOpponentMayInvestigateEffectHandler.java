package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMayInvestigateEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Wernog's sequential opponent investigate choices. */
@Component
@RequiredArgsConstructor
public class EachOpponentMayInvestigateEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentMayInvestigateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachOpponentMayInvestigateEffect investigateEffect =
                (EachOpponentMayInvestigateEffect) effect;
        UUID controllerId = investigateEffect.abilityControllerId() != null
                ? investigateEffect.abilityControllerId()
                : entry.getControllerId();
        List<UUID> opponents = investigateEffect.remainingOpponentIds() == null
                ? apnapOpponents(gameData, controllerId)
                : new ArrayList<>(investigateEffect.remainingOpponentIds());
        opponents.removeIf(id -> !gameData.playerIds.contains(id));

        if (opponents.isEmpty()) {
            createClues(gameData, entry.getCard(), controllerId,
                    investigateEffect.investigatedOpponentCount() + 1);
            return;
        }

        promptNext(gameData, entry.getCard(), new EachOpponentMayInvestigateEffect(
                opponents, controllerId, investigateEffect.investigatedOpponentCount()));
    }

    public void completeChoice(GameData gameData, PendingMayAbility ability,
                               EachOpponentMayInvestigateEffect effect, boolean accepted) {
        UUID controllerId = effect.abilityControllerId();
        int investigatedCount = effect.investigatedOpponentCount();
        if (accepted) {
            investigatedCount++;
            createClues(gameData, ability.sourceCard(), controllerId, ability.controllerId(), 1);
        } else {
            lifeSupport.applyLifeLoss(gameData, ability.controllerId(), 1,
                    ability.sourceCard().getName());
        }

        List<UUID> remaining = new ArrayList<>(effect.remainingOpponentIds());
        remaining.remove(ability.controllerId());
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (!remaining.isEmpty()) {
            promptNext(gameData, ability.sourceCard(), new EachOpponentMayInvestigateEffect(
                    remaining, controllerId, investigatedCount));
            return;
        }

        createClues(gameData, ability.sourceCard(), controllerId, investigatedCount + 1);
    }

    private void promptNext(GameData gameData, Card sourceCard,
                            EachOpponentMayInvestigateEffect effect) {
        UUID opponentId = effect.remainingOpponentIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                sourceCard,
                opponentId,
                List.of(effect),
                sourceCard.getName() + " - Investigate? If you don't, you lose 1 life."));
    }

    private void createClues(GameData gameData, Card sourceCard, UUID sourceControllerId,
                             int amount) {
        createClues(gameData, sourceCard, sourceControllerId, sourceControllerId, amount);
    }

    private void createClues(GameData gameData, Card sourceCard, UUID sourceControllerId,
                             UUID tokenControllerId, int amount) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                sourceControllerId,
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(CreateTokenEffect.ofClueToken(amount))));
        createTokenEffectHandler.resolveForController(gameData, entry,
                CreateTokenEffect.ofClueToken(amount), tokenControllerId);
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
            ordered = rotated;
        }
        return new ArrayList<>(ordered.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList());
    }
}
