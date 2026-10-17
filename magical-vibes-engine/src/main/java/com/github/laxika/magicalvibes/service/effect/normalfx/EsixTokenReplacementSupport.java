package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingTokenCreationReplacement;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfChosenCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EsixFractalBloomEffect;
import com.github.laxika.magicalvibes.model.effect.MirrormindCrownEffect;
import com.github.laxika.magicalvibes.model.effect.MoonlitMeditationEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Esix, Fractal Bloom's and Mirrormind Crown's "first time you would create one or more tokens"
 * replacements, shared by the token-copy effects that offer them.
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

    /**
     * Offers Mirrormind Crown's replacement for {@code amount} tokens if an unused, attached Crown is
     * available and the controller has not created tokens yet this turn, parking the original creation
     * in {@code pendingTokenCreationReplacement} for the re-run of the resolving effect.
     *
     * @return {@code true} when the optional replacement was offered and the caller must stop resolving
     */
    public boolean offerMirrormindCrown(GameData gameData, UUID controllerId, int amount) {
        if (gameData.playersWhoCreatedTokensThisTurn.contains(controllerId)) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent crown : battlefield) {
            if (gameData.tokenCreationReplacementUsedThisTurn.contains(crown.getId())
                    || crown.getAttachedTo() == null
                    || crown.getCard().getEffects(EffectSlot.STATIC).stream()
                    .noneMatch(MirrormindCrownEffect.class::isInstance)
                    || gameQueryService.findPermanentById(gameData, crown.getAttachedTo()) == null) {
                continue;
            }
            gameData.tokenCreationReplacementUsedThisTurn.add(crown.getId());
            gameData.pendingTokenCreationReplacement = new PendingTokenCreationReplacement(
                    crown.getId(), amount, 0, 0, false, false);
            gameData.resolvingMayEffectFromStack = true;
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    crown.getCard(),
                    controllerId,
                    List.of(new CreateTokenCopyOfEquippedCreatureEffect(amount, false, false, crown.getId())),
                    crown.getCard().getName()
                            + " — You may create that many tokens that are copies of the equipped creature."));
            return true;
        }
        return false;
    }

    /**
     * Offers Moonlit Meditation's replacement for {@code amount} tokens if an unused, attached Aura is
     * available and the controller has not created tokens yet this turn, parking the original creation
     * in {@code pendingTokenCreationReplacement} for the re-run of the resolving effect.
     *
     * @return {@code true} when the optional replacement was offered and the caller must stop resolving
     */
    public boolean offerMoonlitMeditation(GameData gameData, UUID controllerId, int amount) {
        if (gameData.playersWhoCreatedTokensThisTurn.contains(controllerId)) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        for (Permanent aura : battlefield) {
            if (gameData.tokenCreationReplacementUsedThisTurn.contains(aura.getId())
                    || aura.getAttachedTo() == null
                    || aura.getCard().getEffects(EffectSlot.STATIC).stream()
                    .noneMatch(MoonlitMeditationEffect.class::isInstance)
                    || gameQueryService.findPermanentById(gameData, aura.getAttachedTo()) == null) {
                continue;
            }
            gameData.tokenCreationReplacementUsedThisTurn.add(aura.getId());
            gameData.pendingTokenCreationReplacement = new PendingTokenCreationReplacement(
                    aura.getId(), amount, 0, 0, true, false);
            gameData.resolvingMayEffectFromStack = true;
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    aura.getCard(),
                    controllerId,
                    List.of(new CreateTokenCopyOfEnchantedPermanentEffect(amount, aura.getId())),
                    aura.getCard().getName()
                            + " — You may create that many tokens that are copies of the enchanted permanent."));
            return true;
        }
        return false;
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
