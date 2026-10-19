package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealHandChooseCardFromItAndExileAllCopiesEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.CardRevealService;
import java.util.List;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevealHandChooseCardFromItAndExileAllCopiesEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final CardRevealService cardRevealService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealHandChooseCardFromItAndExileAllCopiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RevealHandChooseCardFromItAndExileAllCopiesEffect) effect;
        cardRevealService.revealHandToAllPlayers(gameData, entry.getTargetId());
        Predicate<Card> choosable = card -> predicateEvaluationService.matchesCardPredicate(
                card, e.choosableFilter(), null);
        if (gameData.playerHands.getOrDefault(entry.getTargetId(), List.of()).stream().noneMatch(choosable)) {
            // With no card to choose the controller may still search the player's library (for nothing)
            // and have that player shuffle it.
            librarySearchSupport.sendLibrarySearchToPlayer(gameData, entry.getControllerId(),
                    LibrarySearchParams.builder(entry.getControllerId(), List.<Card>of())
                            .targetPlayerId(entry.getTargetId())
                            .canFailToFind(true)
                            .shuffleAfterSelection(true)
                            .build(),
                    "You may search the library; no card was chosen, so there is nothing to find.", true,
                    gameData.playerIdToName.get(entry.getControllerId()) + " may search "
                            + gameData.playerIdToName.get(entry.getTargetId()) + "'s library.");
            return;
        }

        playerInputService.beginRevealHandChooseCardFromItAndExileAllCopiesChoice(
                gameData, entry.getControllerId(), entry.getTargetId(),
                choosable,
                CardPredicateUtils.describeFilter(e.choosableFilter()), entry.getCard(), e.chooseAnyNumber());
    }
}
