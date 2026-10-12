package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingTokenCreationReplacement;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfChosenCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EsixFractalBloomEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Esix, Fractal Bloom's "first time you would create one or more tokens during each of your turns"
 * replacement, shared by every token-creating effect that offers it.
 */
@Component
@RequiredArgsConstructor
public class EsixTokenReplacementSupport {

    private final GameQueryService gameQueryService;

    public boolean isAvailable(GameData gameData, UUID controllerId) {
        return availableEsix(gameData, controllerId) != null;
    }

    /**
     * Offers the replacement for {@code amount} tokens if an unused Esix is available, parking the
     * original creation in {@code pendingTokenCreationReplacement} for the re-run of the resolving effect.
     *
     * @return {@code true} when the optional replacement was offered and the caller must stop resolving
     */
    public boolean offer(GameData gameData, UUID controllerId, int amount, int power, int toughness) {
        Permanent esix = availableEsix(gameData, controllerId);
        if (esix == null) {
            return false;
        }
        gameData.tokenCreationReplacementUsedThisTurn.add(esix.getId());
        gameData.pendingTokenCreationReplacement = new PendingTokenCreationReplacement(
                esix.getId(), amount, power, toughness, false, true);
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                esix.getCard(),
                controllerId,
                List.of(new CreateTokenCopyOfChosenCreatureEffect(amount, esix.getId())),
                esix.getCard().getName()
                        + " — You may instead choose a creature other than Esix and create that many tokens that are copies of it."));
        return true;
    }

    private Permanent availableEsix(GameData gameData, UUID controllerId) {
        if (!controllerId.equals(gameData.activePlayerId)
                || gameData.playersWhoCreatedTokensThisTurn.contains(controllerId)) {
            return null;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return null;
        }
        for (Permanent permanent : battlefield) {
            if (gameData.tokenCreationReplacementUsedThisTurn.contains(permanent.getId())
                    || permanent.isLosesAllAbilitiesUntilEndOfTurn()
                    || permanent.isStaticEffectSuppressed(EsixFractalBloomEffect.class)
                    || permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .noneMatch(EsixFractalBloomEffect.class::isInstance)) {
                continue;
            }
            boolean hasOtherCreature = gameData.orderedPlayerIds.stream()
                    .flatMap(playerId -> gameData.playerBattlefields
                            .getOrDefault(playerId, List.of()).stream())
                    .anyMatch(candidate -> !candidate.getId().equals(permanent.getId())
                            && gameQueryService.isCreature(gameData, candidate));
            if (hasOtherCreature) {
                return permanent;
            }
        }
        return null;
    }
}
