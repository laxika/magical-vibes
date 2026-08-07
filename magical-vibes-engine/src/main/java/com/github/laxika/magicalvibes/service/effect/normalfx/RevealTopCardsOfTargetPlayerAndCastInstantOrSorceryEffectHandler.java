package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOfTargetPlayerAndCastInstantOrSorceryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "Target player reveals the top N cards of their library. You may cast an instant or sorcery spell
 * from among them without paying its mana cost. Then that player puts the rest into their
 * graveyard." (Talent of the Telepath, whose spell mastery raises the limit to two.)
 *
 * <p>The revealed cards leave the library immediately and are held by
 * {@link RevealedFreeCastSupport} until the casting decisions are done, so a cast card never passes
 * through the graveyard on its way to the stack.
 */
@Component
@RequiredArgsConstructor
public class RevealTopCardsOfTargetPlayerAndCastInstantOrSorceryEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final RevealedFreeCastSupport revealedFreeCastSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsOfTargetPlayerAndCastInstantOrSorceryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardsOfTargetPlayerAndCastInstantOrSorceryEffect e =
                (RevealTopCardsOfTargetPlayerAndCastInstantOrSorceryEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerDecks.containsKey(targetPlayerId)) return;

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        int actual = Math.min(e.count(), deck.size());
        if (actual == 0) {
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(": " + targetName + "'s library is empty.").build());
            return;
        }

        List<Card> revealed = LibraryRevealSupport.takeTopCards(deck, actual);

        GameLog.Builder revealLog = GameLog.builder().text(targetName + " reveals ");
        for (int i = 0; i < revealed.size(); i++) {
            if (i > 0) revealLog.text(", ");
            revealLog.card(revealed.get(i));
        }
        revealLog.text(".");
        gameLogService.append(gameData, revealLog.build());

        int casts = 1;
        if (e.extraCastCondition() != null && conditionEvaluationService.isMet(
                gameData, e.extraCastCondition(), ConditionContext.forStackEntry(entry))) {
            casts = 2;
        }

        revealedFreeCastSupport.offerOrDump(gameData, targetPlayerId, entry.getControllerId(),
                revealed, casts);
    }
}
