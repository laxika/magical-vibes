package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adds an emblem to {@code gameData.emblems} under the control of each player {@link SubjectResolutionService} finds
 * for the effect's subject. The effect's static effects and
 * abilities are stored verbatim; nothing here resolves or registers them: the trigger readers read the emblem's
 * triggered abilities from the command zone.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateEmblemEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateEmblemEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateEmblemEffect emblemEffect = (CreateEmblemEffect) effect;

        for (UUID recipientId : subjectResolutionService.players(gameData, entry, effect, emblemEffect.player())) {
            createEmblem(gameData, recipientId, entry.getCard() != null ? entry.getCard().getName() : "",
                    emblemEffect);
        }
    }

    /**
     * Gives {@code recipientId} an emblem described by {@code emblemEffect}, attributed to the card
     * named {@code sourceName}. Shared with the as-enters form, which has no stack entry.
     */
    public void createEmblem(GameData gameData, UUID recipientId, String sourceName,
                             CreateEmblemEffect emblemEffect) {
        if (!gameData.playerIds.contains(recipientId)) {
            return;
        }
        String playerName = gameData.playerIdToName.get(recipientId);

        Card emblemSource = new Card();
        emblemSource.setName(sourceName);
        emblemSource.setType(CardType.EMBLEM);
        emblemSource.setCardText(emblemEffect.reminderText());
        emblemSource.setOwnerId(recipientId);
        emblemSource.freeze();
        gameData.emblems.add(new Emblem(recipientId, emblemEffect.staticEffects(), emblemSource,
                emblemEffect.activatedAbilities(), gameData.nextTimestamp(), emblemEffect.triggeredAbilities()));

        gameLogService.append(gameData, GameLog.text(
                playerName + " gets an emblem with \"" + emblemEffect.reminderText() + "\"."));
        log.info("Game {} - {} gets an emblem from {}", gameData.id, playerName, sourceName);
    }
}
