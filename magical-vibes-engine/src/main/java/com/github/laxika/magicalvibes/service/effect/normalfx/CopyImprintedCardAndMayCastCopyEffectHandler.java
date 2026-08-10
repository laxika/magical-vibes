package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolution of Elite Arcanist's "{X}, {T}: Copy the exiled card. You may cast the copy without
 * paying its mana cost." The copy is created straight away and parked in exile so the shared
 * free-cast queue can put it on the stack; the imprinted card itself never leaves exile. The
 * accept/decline half lives in {@code CopyImprintedCardAndMayCastCopyHandler}.
 */
@Component
@RequiredArgsConstructor
public class CopyImprintedCardAndMayCastCopyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final CopySupport copySupport;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyImprintedCardAndMayCastCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        CopyImprintedCardAndMayCastCopyEffect copyEffect =
                (CopyImprintedCardAndMayCastCopyEffect) effect;

        // The source may have left the battlefield since activation; entry.getCard() keeps the
        // imprint pointer alive in that case (same fallback as the token-copy handler).
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Card imprintedCard = copyEffect.copyOtherExiledCard()
                ? findOtherExiledCard(gameData, entry, copyEffect)
                : sourcePermanent != null
                        ? gameData.getImprintedCard(sourcePermanent.getCard())
                        : gameData.getImprintedCard(entry.getCard());

        if (imprintedCard == null || gameData.findExiledCard(imprintedCard.getId()) == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " has no exiled card to copy."));
            return;
        }

        Card copy = copySupport.createCopyCard(imprintedCard);
        exileService.exileCard(gameData, controllerId, copy);

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                copy,
                controllerId,
                List.of(copyEffect),
                "Cast the copy of " + copy.getName() + " without paying its mana cost?",
                copy.getId()
        ));
    }

    private Card findOtherExiledCard(GameData gameData, StackEntry entry,
                                     CopyImprintedCardAndMayCastCopyEffect effect) {
        List<Card> exiledCards = gameData.getCardsExiledByPermanent(entry.getSourcePermanentId());
        if (exiledCards.size() < 2) {
            return null;
        }

        String triggeringName = null;
        if (effect.triggeringCardId() != null) {
            for (StackEntry stackEntry : gameData.stack) {
                if (stackEntry.getCard().getId().equals(effect.triggeringCardId())) {
                    triggeringName = stackEntry.getCard().getName();
                    break;
                }
            }
        }

        Card first = exiledCards.getFirst();
        Card second = exiledCards.get(1);
        if (triggeringName != null && first.getName().equals(triggeringName)
                && !second.getName().equals(triggeringName)) {
            return second;
        }
        return first;
    }
}
