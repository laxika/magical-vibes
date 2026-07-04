package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect) effect);
    }

    private void doResolve(
            GameData gameData, StackEntry entry,
            DestroyTargetPermanentAndControllerSearchesLibraryToBattlefieldEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Find the controller of the targeted permanent before destruction
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null) {
            return;
        }

        // Attempt to destroy the permanent
        if (permanentRemovalService.tryDestroyPermanent(gameData, target, false)) {
            String logEntry = target.getCard().getName() + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} is destroyed by {}", gameData.id, target.getCard().getName(), entry.getCard().getName());
        }

        // The target's controller searches their library
        String desc = CardPredicateUtils.describeFilter(effect.searchFilter());
        String descPlural = desc.replace(" card", " cards");
        String tappedSuffix = effect.tapped() ? " tapped" : "";
        String prompt = effect.may()
                ? "You may search your library for a " + desc + " and put it onto the battlefield" + tappedSuffix + "."
                : "Search your library for a " + desc + " and put it onto the battlefield" + tappedSuffix + ".";

        librarySearchSupport.performLibrarySearch(
                gameData,
                targetControllerId,
                card -> predicateEvaluationService.matchesCardPredicate(card, effect.searchFilter(), null),
                descPlural,
                prompt,
                false,
                effect.may(),
                effect.tapped()
                        ? LibrarySearchDestination.BATTLEFIELD_TAPPED
                        : LibrarySearchDestination.BATTLEFIELD
        );
    }
}
