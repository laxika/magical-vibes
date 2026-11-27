package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingTokenCreationReplacement;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves "each opponent creates a token that's a copy of it" (Life of the Party). Each of those players creates their
 * token, so their "the first time you would create one or more tokens" replacements (Mirrormind Crown, Moonlit
 * Meditation) are offered to them (CR 111.2), one player at a time in turn order: the effect is resolved again after
 * each answer and goes on with the players after the one who answered, whose tokens are not yet created.
 */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfSourceForEachOtherPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;
    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfSourceForEachOtherPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenCopyOfSourceForEachOtherPlayerEffect copyEffect =
                (CreateTokenCopyOfSourceForEachOtherPlayerEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        boolean sourceOnBattlefield = source != null;
        if (!sourceOnBattlefield) {
            // The source left before the trigger resolved: copy it as it last existed on the battlefield.
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null || source.getCard().isToken()
                || sourceOnBattlefield && !gameQueryService.isCreature(gameData, source)) return;

        UUID sourceControllerId = sourceOnBattlefield
                ? gameQueryService.findPermanentController(gameData, source.getId())
                : entry.getControllerId();
        if (sourceControllerId == null) return;

        Card copied = PermanentCopierService.cardToCopy(source);
        List<UUID> creators = copyEffect.creators() != null ? copyEffect.creators()
                : gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(sourceControllerId))
                .toList();
        int next = 0;
        PendingTokenCreationReplacement answered = gameData.pendingTokenCreationReplacement;
        if (answered != null && creators.contains(answered.creatorId())) {
            next = creators.indexOf(answered.creatorId());
        }
        for (int i = next; i < creators.size(); i++) {
            UUID creatorId = creators.get(i);
            if (tokenCopyHandler.replaceFirstTokenCreation(gameData, entry, 1, creatorId)) {
                if (gameData.pendingTokenCreationReplacement != null) {
                    // The offer's answer resolves this effect again, from this player on.
                    return;
                }
                if (gameData.interaction.isAwaitingInput()) {
                    // The accepted replacement asks a choice of its own (Esix's creature) and the resolution goes on
                    // with its next step once it is made: the players after this one create theirs in that step.
                    if (i + 1 < creators.size() && entry.getResolvingEffectIndex() >= 0) {
                        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                                List.of(copyEffect.forCreators(creators.subList(i + 1, creators.size()))));
                    }
                    return;
                }
                continue;
            }
            tokenCopySupport.createTokenCopies(gameData, entry, List.of(copied), source, creatorId,
                    copyEffect.copyProfile());
        }
    }
}
