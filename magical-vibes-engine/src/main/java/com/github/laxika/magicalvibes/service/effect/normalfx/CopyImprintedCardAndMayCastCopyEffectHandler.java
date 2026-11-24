package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolution of "copy the exiled card. You may cast the copy": each copy is created straight away and parked in exile
 * for the may-choice; the card itself never leaves exile. Without a card subject it copies the source's imprinted and
 * exiled-with cards (Elite Arcanist's "{X}, {T}: Copy the exiled card"); with one, the cards the subject names in exile,
 * a graveyard or a hand (the cards an earlier exile step of the resolution exiled, a card chosen from a revealed hand). The accept/decline half lives in
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
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    /** The zones a card subject's cards are copied from. */
    private static final Set<Zone> COPIED_ZONES = Set.of(Zone.EXILE, Zone.GRAVEYARD, Zone.HAND);

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
     * Copies each card the subject names in exile, a graveyard or a hand and parks the copy with a may-choice to cast
     * it, the first card's offer first; the offer keeps the source permanent for a "if you do" rider on the source.
     * With {@code castsAtMost}, the copies of spell cards are offered in one ordered choice instead (CR 608.2g); a copy
     * of a land card can't be cast and is not made.
     */
    private void offerCopiesOfSubjectCards(GameData gameData, StackEntry entry,
                                           CopyImprintedCardAndMayCastCopyEffect copyEffect) {
        List<Card> namedCards = subjectResolutionService.locatedCards(gameData, entry, copyEffect, copyEffect.cards())
                .stream()
                .filter(located -> COPIED_ZONES.contains(located.zone()))
                .map(SubjectResolutionService.LocatedCard::card)
                .toList();
        if (copyEffect.castsAtMost() > 0) {
            offerChoiceAmongCopies(gameData, entry, copyEffect, namedCards);
            return;
        }
        for (Card namedCard : namedCards.reversed()) {
            Card copy = copySupport.createCopyCard(namedCard);
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

    /**
     * "You may cast up to N / any number of the copies": the copies of the named spell cards, parked in exile, are
     * offered in one choice that also orders the casts; the free-cast queue casts the chosen ones and the others
     * cease to exist (CR 707.10a).
     */
    private void offerChoiceAmongCopies(GameData gameData, StackEntry entry,
                                        CopyImprintedCardAndMayCastCopyEffect copyEffect, List<Card> namedCards) {
        UUID controllerId = entry.getControllerId();
        List<UUID> copyIds = new ArrayList<>();
        for (Card namedCard : namedCards) {
            if (!isSpell(namedCard)) {
                continue;
            }
            Card copy = copySupport.createCopyCard(namedCard);
            exileService.exileCard(gameData, controllerId, copy);
            copyIds.add(copy.getId());
        }
        if (copyIds.isEmpty()) {
            return;
        }
        int atMost = copyEffect.castsAtMost();
        String spells = atMost == Integer.MAX_VALUE ? "any number of the copies"
                : "up to " + atMost + " of the copies";
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ImprovisationCapstoneCastChoice(
                controllerId, copyIds, Math.min(atMost, copyIds.size()),
                "You may cast " + spells + " without paying their mana costs.", true));
    }

    /** An instant, a sorcery or a nonland permanent card: a land card is played, not cast. */
    private static boolean isSpell(Card card) {
        if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
            return true;
        }
        return card.getType().isPermanentType() && !card.hasType(CardType.LAND);
    }
}
