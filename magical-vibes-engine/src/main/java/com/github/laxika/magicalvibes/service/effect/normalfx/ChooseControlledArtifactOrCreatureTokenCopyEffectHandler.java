package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseControlledArtifactOrCreatureTokenCopyEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the non-targeting copy mode of Season of Weaving. */
@Component
@RequiredArgsConstructor
public class ChooseControlledArtifactOrCreatureTokenCopyEffectHandler implements NormalEffectHandlerBean {

    private static final CreateTokenCopyOfTargetPermanentEffect TOKEN_PROFILE =
            new CreateTokenCopyOfTargetPermanentEffect();

    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseControlledArtifactOrCreatureTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        List<UUID> candidates = gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isArtifact(gameData, permanent)
                        || gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();

        if (candidates.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                    " resolves but its controller controls no artifact or creature to copy."));
            return;
        }
        if (candidates.size() == 1) {
            createCopy(gameData, controllerId, sourceCard, candidates.getFirst());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ChooseControlledArtifactOrCreatureToCopy(sourceCard, controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, candidates,
                sourceCard.getName() + " - Choose an artifact or creature you control to be copied.");
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.ChooseControlledArtifactOrCreatureToCopy context) {
        createCopy(gameData, context.controllerId(), context.sourceCard(), permanentId);
    }

    private void createCopy(GameData gameData, UUID controllerId, Card sourceCard, UUID chosenPermanentId) {
        StackEntry copyEntry = new StackEntry(sourceCard, controllerId);
        copyEntry.setTargetId(chosenPermanentId);
        tokenCopyHandler.resolve(gameData, copyEntry, TOKEN_PROFILE);
    }
}
