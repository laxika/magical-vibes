package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOwnGraveyardInstantsOrSorceriesAndMayCastCopiesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the overloaded Mizzix's Mastery graveyard exile and copy offers. */
@Component
@RequiredArgsConstructor
public class ExileOwnGraveyardInstantsOrSorceriesAndMayCastCopiesEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOwnGraveyardInstantsOrSorceriesAndMayCastCopiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null || graveyard.isEmpty()) {
            return;
        }

        List<Card> matchingCards = new ArrayList<>();
        for (Card card : graveyard) {
            if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
                matchingCards.add(card);
            }
        }

        if (matchingCards.isEmpty()) {
            return;
        }

        List<UUID> copyIds = new ArrayList<>();
        for (Card card : matchingCards) {
            permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, card.getId());
            exileService.exileCard(gameData, controllerId, card);
            gameLogService.append(gameData, GameLog.isExiled(card));

            Card copy = copySupport.createCopyCard(card);
            exileService.exileCard(gameData, controllerId, copy);
            copyIds.add(copy.getId());
        }

        ExileTargetCardFromGraveyardAndMayCastCopyEffect mayCastEffect =
                new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                        null, GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
        for (int i = copyIds.size() - 1; i >= 0; i--) {
            UUID copyId = copyIds.get(i);
            Card copy = gameQueryService.findCardInExileById(gameData, copyId);
            if (copy != null) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        copy,
                        controllerId,
                        List.of(mayCastEffect),
                        "Cast the copy of " + copy.getName() + " without paying its mana cost?",
                        copyId));
            }
        }
    }
}
