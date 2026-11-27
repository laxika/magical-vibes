package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PendingTokenCreationReplacement;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfChosenCreatureEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EsixFractalBloomEffect;
import com.github.laxika.magicalvibes.model.effect.FirstTokenCreationCopiesAttachedPermanentEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * The "first time you would create one or more tokens" replacements, shared by the token-creating effects that
 * offer them: Esix, Fractal Bloom's, and {@link FirstTokenCreationCopiesAttachedPermanentEffect} (Mirrormind Crown,
 * Moonlit Meditation).
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
                esix.getId(), amount, power, toughness, true, controllerId);
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                esix.getCard(),
                controllerId,
                List.of(new CreateTokenCopyOfChosenCreatureEffect(amount, esix.getId())),
                FaceDownVisibility.publicName(gameData, esix)
                        + " — You may instead choose a creature other than Esix and create that many tokens that are copies of it."));
        return true;
    }

    /**
     * Whether an Aura or Equipment of {@code controllerId}'s with {@link FirstTokenCreationCopiesAttachedPermanentEffect}
     * could replace their next token creation (Mirrormind Crown, Moonlit Meditation).
     */
    public boolean isAttachedCopyAvailable(GameData gameData, UUID controllerId) {
        return availableAttachedCopy(gameData, controllerId) != null;
    }

    /**
     * Offers the replacement of {@link FirstTokenCreationCopiesAttachedPermanentEffect} for {@code amount} tokens if
     * one of {@code controllerId}'s Auras or Equipment has it, still has its abilities (CR 613.1f), is attached, has not
     * offered it this turn and the controller has not created tokens yet this turn, parking the original creation in
     * {@code pendingTokenCreationReplacement} for the re-run of the resolving effect.
     *
     * @return {@code true} when the optional replacement was offered and the caller must stop resolving
     */
    public boolean offerAttachedCopy(GameData gameData, UUID controllerId, int amount, int power, int toughness) {
        Permanent source = availableAttachedCopy(gameData, controllerId);
        if (source == null) {
            return false;
        }
        Permanent attached = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
        gameData.tokenCreationReplacementUsedThisTurn.add(source.getId());
        gameData.pendingTokenCreationReplacement = new PendingTokenCreationReplacement(
                source.getId(), amount, power, toughness, false, controllerId);
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                source.getCard(),
                controllerId,
                List.of(new CreateTokenCopyOfTargetPermanentEffect(new Fixed(amount)).ofNamed(attached.getId())),
                FaceDownVisibility.publicName(gameData, source)
                        + " — You may instead create that many tokens that are copies of "
                        + FaceDownVisibility.publicName(gameData, attached) + "."));
        return true;
    }

    private Permanent availableAttachedCopy(GameData gameData, UUID controllerId) {
        if (gameData.playersWhoCreatedTokensThisTurn.contains(controllerId)) {
            return null;
        }
        for (var active : gameQueryService.activeStaticEffects(gameData, controllerId,
                FirstTokenCreationCopiesAttachedPermanentEffect.class)) {
            Permanent source = active.source();
            if (!gameData.tokenCreationReplacementUsedThisTurn.contains(source.getId())
                    && source.getAttachedTo() != null
                    && gameQueryService.findPermanentById(gameData, source.getAttachedTo()) != null) {
                return source;
            }
        }
        return null;
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
                    || gameData.losesAllAbilitiesByEffect(permanent)
                    || gameData.removesOwnStaticAbility(permanent.getId(), EsixFractalBloomEffect.class)
                    || permanent.getCard().getStaticEffects().stream()
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
