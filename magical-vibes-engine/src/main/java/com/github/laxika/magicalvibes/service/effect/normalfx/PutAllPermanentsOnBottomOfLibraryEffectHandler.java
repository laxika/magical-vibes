package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllPermanentsOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutAllPermanentsOnBottomOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutAllPermanentsOnBottomOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutAllPermanentsOnBottomOfLibraryEffect) effect;
        List<Permanent> toBottom = new ArrayList<>();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(perm, e.filter(), filterContext)) {
                    toBottom.add(perm);
                }
            }
        });

        Map<UUID, Integer> librarySizesBefore = new LinkedHashMap<>();
        gameData.playerDecks.forEach((playerId, library) -> librarySizesBefore.put(playerId, library.size()));

        for (Permanent perm : permanentRemovalService.removeAllToLibraryBottom(gameData, toBottom)) {
            gameLogService.append(gameData, GameLog.cardThen(perm.getCard(), " is put on the bottom of its owner's library."));
            log.info("Game {} - {} put on bottom of library by {}",
                    gameData.id, perm.getCard().getName(), entry.getCard().getName());
        }

        // CR 401.4: cards put into the same library at the same time are arranged by their owner.
        librarySizesBefore.forEach((ownerId, sizeBefore) -> {
            List<Card> library = gameData.playerDecks.get(ownerId);
            if (library.size() - sizeBefore < 2) {
                return;
            }
            PendingInteraction.LibraryReorder reorder = new PendingInteraction.LibraryReorder(
                    ownerId, new ArrayList<>(library.subList(sizeBefore, library.size())), true, ownerId,
                    "Put these cards on the bottom of your library in any order "
                            + "(first chosen will be closest to the top).",
                    0, null, false, false, Zone.LIBRARY, null);
            if (gameData.interaction.isAwaitingInput()) {
                gameData.queueInteraction(reorder);
            } else {
                interactionHandlerRegistry.begin(gameData, reorder);
            }
        });
    }
}
