package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EncoreEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Encore: for each opponent, a token copy of the exiled card that attacks that opponent this turn if able. The copies
 * are one ordered token creation event (CR 616.1); every token it creates, added ones included, gains haste and is
 * sacrificed at the beginning of the next end step.
 */
@Component
@RequiredArgsConstructor
public class EncoreEffectHandler implements NormalEffectHandlerBean {

    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EncoreEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getCard() == null) {
            return;
        }
        List<UUID> opponentIds = gameData.orderedPlayerIds.stream()
                .filter(playerId -> gameData.playerIds.contains(playerId)
                        && !playerId.equals(entry.getControllerId()))
                .toList();
        CreateTokenCopyOfTargetPermanentEffect copyEffect = new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(), null, null, Map.of(), false, false, true, false, false,
                false, null, Set.of(), false, List.of(), List.of(), false, false,
                new Fixed(1), false, Set.of(), false,
                List.of());
        // "They gain haste" is granted to the tokens, not part of the copy (CR 707.2).
        tokenCopySupport.createTokenCopiesForEachOpponent(gameData, entry, List.of(entry.getCard()),
                entry.getControllerId(), copyEffect, opponentIds,
                token -> token.getPersistentGrantedKeywords().add(Keyword.HASTE),
                (opponentId, token) -> {
                    token.setMustAttackThisTurn(true);
                    token.setMustAttackTargetId(opponentId);
                });
    }
}
