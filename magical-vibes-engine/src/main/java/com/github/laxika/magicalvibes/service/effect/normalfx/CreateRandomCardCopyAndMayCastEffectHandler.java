package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateRandomCardCopyAndMayCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCopyWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a fixed random-card copy effect and offers the copy for free casting. */
@Component
@RequiredArgsConstructor
public class CreateRandomCardCopyAndMayCastEffectHandler implements NormalEffectHandlerBean {

    private final CopySupport copySupport;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateRandomCardCopyAndMayCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateRandomCardCopyAndMayCastEffect randomEffect =
                (CreateRandomCardCopyAndMayCastEffect) effect;
        Card selectedCard = randomEffect.cardFactories()
                .get(ThreadLocalRandom.current().nextInt(randomEffect.cardFactories().size()))
                .get();
        Card copy = copySupport.createCopyCard(selectedCard);
        exileService.exileCard(gameData, entry.getControllerId(), copy);
        gameLogService.append(gameData, GameLog.cardTextCard(
                entry.getCard(), " creates a copy of ", copy, "."));
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                copy,
                entry.getControllerId(),
                List.of(new MayCastCopyWithoutPayingManaCostEffect()),
                "Cast the copy of " + copy.getName() + " without paying its mana cost?",
                copy.getId()));
    }
}
