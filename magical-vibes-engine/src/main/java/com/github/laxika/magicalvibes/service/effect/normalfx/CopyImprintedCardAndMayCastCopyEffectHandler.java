package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolution of "copy the exiled card. You may cast the copy": each copy is created straight away and parked in exile
 * for the may-choice; the card itself never leaves exile. Without a card subject it copies the source's imprinted and
 * exiled-with cards (Elite Arcanist's "{X}, {T}: Copy the exiled card"); with one, the cards the subject names in exile
 * (the cards an earlier exile step of the resolution exiled). The accept/decline half lives in
 * {@code CopyImprintedCardAndMayCastCopyHandler}.
 */
@Component
@RequiredArgsConstructor
public class CopyImprintedCardAndMayCastCopyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final CopySupport copySupport;
    private final ExileService exileService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyImprintedCardAndMayCastCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        CopyImprintedCardAndMayCastCopyEffect copyEffect =
                (CopyImprintedCardAndMayCastCopyEffect) effect;
        if (copyEffect.cards() != null) {
            offerCopiesOfSubjectCards(gameData, entry, copyEffect);
            return;
        }

        // The source may have left the battlefield since activation; entry.getCard() keeps the
        // imprint pointer alive in that case (same fallback as the token-copy handler).
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        List<Card> imprintedCards = new ArrayList<>();
        if (entry.getLinkedExiledCardId() != null) {
            // The card the source's linked ability exiled as this ability triggered, recorded on the entry (a delayed
            // ability a spell registered: Surge to Victory's "the exiled card", CR 603.7c).
            var linked = gameData.findExiledCard(entry.getLinkedExiledCardId());
            if (linked != null) {
                imprintedCards.add(linked.card());
            }
        } else if (copyEffect.copyOtherExiledCard()) {
            // "Copy the other": every card exiled with the source besides the first one with the name of the cast card,
            // read as the ability resolves (a doubled imprint trigger links more than two). None has the name: nothing.
            StackEntry spell = CopySupport.triggeringSpell(gameData, entry);
            List<Card> exiled = gameData.getCardsExiledByPermanent(entry.getSourcePermanentId());
            Card matching = spell == null || spell.getCard() == null ? null : exiled.stream()
                    .filter(card -> card.getName().equals(spell.getCard().getName()))
                    .findFirst().orElse(null);
            if (matching != null) {
                exiled.stream().filter(card -> card != matching).forEach(imprintedCards::add);
            }
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

    /**
     * Copies each card the subject names in exile and parks the copy with a may-choice to cast it, the first card's
     * offer first; the offer keeps the source permanent for a "if you do" rider on the source.
     */
    private void offerCopiesOfSubjectCards(GameData gameData, StackEntry entry,
                                           CopyImprintedCardAndMayCastCopyEffect copyEffect) {
        List<Card> exiledCards = subjectResolutionService.locatedCards(gameData, entry, copyEffect, copyEffect.cards())
                .stream()
                .filter(located -> located.zone() == Zone.EXILE)
                .map(SubjectResolutionService.LocatedCard::card)
                .toList();
        for (Card exiledCard : exiledCards.reversed()) {
            Card copy = copySupport.createCopyCard(exiledCard);
            exileService.exileCard(gameData, entry.getControllerId(), copy);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    copy,
                    entry.getControllerId(),
                    List.of(copyEffect),
                    "Cast the copy of " + copy.getName() + "?",
                    copy.getId(),
                    null,
                    entry.getSourcePermanentId()));
        }
    }
}
