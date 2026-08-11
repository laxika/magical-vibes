package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect}: every player, in
 * APNAP order (CR 101.4), may search their library for up to {@code count} basic land cards, put
 * them onto the battlefield, then shuffle. The per-player picks are queued on the shared
 * {@link LibrarySearchFollowUp.BasicLandSearchQueue} with no sacrifice half and driven by
 * {@link BasicLandSearchQueueSupport}.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final BasicLandSearchQueueSupport basicLandSearchQueueSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect search =
                (EachPlayerMaySearchLibraryForBasicLandsToBattlefieldEffect) effect;
        int count = amountEvaluationService.evaluate(gameData, search.count(),
                AmountContext.forStackEntry(entry, null));
        if (count <= 0) {
            return;
        }

        List<LibrarySearchFollowUp.BasicLandsPick> picks = basicLandSearchQueueSupport.apnapOrder(gameData).stream()
                .map(playerId -> new LibrarySearchFollowUp.BasicLandsPick(playerId, count, search.enterTapped()))
                .toList();

        basicLandSearchQueueSupport.advance(gameData, LibrarySearchFollowUp.basicLandSearches(picks, List.of()));
    }
}
