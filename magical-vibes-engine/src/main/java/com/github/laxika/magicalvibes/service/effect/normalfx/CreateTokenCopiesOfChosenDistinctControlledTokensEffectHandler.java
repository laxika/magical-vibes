package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfChosenDistinctControlledTokensEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenCopiesOfChosenDistinctControlledTokensEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopiesOfChosenDistinctControlledTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        List<UUID> validIds = battlefield.stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> !gameQueryService.cantBeChosen(gameData, permanent))
                .filter(permanent -> gameQueryService.isArtifact(gameData, permanent)
                        || gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
        if (validIds.isEmpty()) {
            return;
        }

        int maxCount = validIds.size();
        playerInputService.beginMultiPermanentChoice(
                gameData,
                entry.getControllerId(),
                validIds,
                maxCount,
                new MultiPermanentChoiceContext.CreateTokenCopiesOfChosenDistinctControlledTokens(),
                FaceDownVisibility.sourceName(gameData, entry) + " — Choose any number of artifact and/or creature tokens with different names.");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds, StackEntry entry) {
        List<Card> sourceCards = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent == null || !permanent.getCard().isToken()
                    || (!gameQueryService.isArtifact(gameData, permanent)
                    && !gameQueryService.isCreature(gameData, permanent))) {
                continue;
            }
            if (names.add(permanent.getCard().getName())) {
                sourceCards.add(PermanentCopierService.cardToCopy(permanent));
            }
        }

        // "For each of them, create a token that's a copy of it": one action on several objects, processed simultaneously (CR 608.2f).
        tokenCopySupport.createTokenCopies(gameData, entry, sourceCards, null,
                new CreateTokenCopyOfTargetPermanentEffect());
    }
}
