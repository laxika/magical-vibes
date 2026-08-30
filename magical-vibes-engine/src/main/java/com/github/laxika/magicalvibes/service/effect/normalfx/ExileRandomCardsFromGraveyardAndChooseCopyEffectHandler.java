package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCardsFromGraveyardAndChooseCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Capricious Hellraiser's random graveyard exile and copy choice. */
@Component
@RequiredArgsConstructor
public class ExileRandomCardsFromGraveyardAndChooseCopyEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomCardsFromGraveyardAndChooseCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (ExileRandomCardsFromGraveyardAndChooseCopyEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(controllerId, List.of());
        if (graveyard.isEmpty()) {
            return;
        }

        List<Card> selectedCards = new ArrayList<>(graveyard);
        Collections.shuffle(selectedCards);
        List<UUID> eligibleCardIds = new ArrayList<>();
        int selectedCount = Math.min(copyEffect.count(), selectedCards.size());
        for (Card card : selectedCards.subList(0, selectedCount)) {
            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, card.getId());
            exileService.exileCard(gameData, controllerId, card);
            gameLogService.append(gameData, GameLog.isExiled(card));
            if (copyEffect.filter() == null
                    || predicateEvaluationService.matchesCardPredicate(
                    card, copyEffect.filter(), entry.getCard().getId())) {
                eligibleCardIds.add(card.getId());
            }
        }

        if (!eligibleCardIds.isEmpty()) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.ExiledSpellCopyChoice(
                            controllerId, eligibleCardIds, 1, true,
                            "a noncreature, nonland card"));
        }
    }
}
