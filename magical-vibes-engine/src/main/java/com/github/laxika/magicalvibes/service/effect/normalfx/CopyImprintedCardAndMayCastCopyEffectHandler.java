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
import java.util.ArrayList;
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
        List<Card> imprintedCards = new ArrayList<>();
        if (copyEffect.boundImprintedCardId() != null) {
            var bound = gameData.findExiledCard(copyEffect.boundImprintedCardId());
            if (bound != null) {
                imprintedCards.add(bound.card());
            }
        } else if (copyEffect.copyOtherExiledCard()) {
            // "Copy the other": every card exiled with the source besides the one that matched the cast
            // card (a doubled imprint trigger links more than two).
            gameData.getCardsExiledByPermanent(entry.getSourcePermanentId()).stream()
                    .filter(card -> !card.getId().equals(copyEffect.matchingExiledCardId()))
                    .forEach(imprintedCards::add);
        } else {
            // A doubled imprint trigger exiles several cards; the ability copies each of them.
            Card imprinted = sourcePermanent != null
                    ? gameData.getImprintedCard(sourcePermanent.getCard())
                    : gameData.getImprintedCard(entry.getCard());
            if (imprinted != null) {
                imprintedCards.add(imprinted);
            }
            for (Card exiled : gameData.getCardsExiledByPermanent(entry.getSourcePermanentId())) {
                if (imprintedCards.stream().noneMatch(card -> card.getId().equals(exiled.getId()))) {
                    imprintedCards.add(exiled);
                }
            }
        }
        imprintedCards.removeIf(card -> gameData.findExiledCard(card.getId()) == null);

        if (imprintedCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " has no exiled card to copy."));
            return;
        }

        for (Card imprintedCard : imprintedCards.reversed()) {
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
    }
}
