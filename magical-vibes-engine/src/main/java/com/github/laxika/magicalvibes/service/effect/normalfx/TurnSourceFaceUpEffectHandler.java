package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TurnSourceFaceUpEffect;
import com.github.laxika.magicalvibes.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class TurnSourceFaceUpEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ObjectProvider<GameService> gameServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TurnSourceFaceUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = subjectResolutionService.permanent(
                gameData, entry, effect, ((TurnSourceFaceUpEffect) effect).subject());
        if (source == null || !source.isFaceDown()) {
            return;
        }
        // A face-down permanent represented by an instant or sorcery card is revealed and stays face down
        // (CR 701.40g); its turned-face-up abilities don't trigger.
        Card card = source.getOriginalCard();
        if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
            gameLogService.appendRevealing(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(controllerId) + " reveals ", card, " and leaves it face down."));
            return;
        }
        gameServiceProvider.getObject().turnPermanentFaceUpWithoutPayingManaCost(gameData, source);
    }
}
