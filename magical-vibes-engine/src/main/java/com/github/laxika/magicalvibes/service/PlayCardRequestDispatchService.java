package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * The single translation from a {@link PlayCardRequest} onto {@link GameService} cast calls,
 * shared by the backend message handler and the AI's action adapter. Every request field is
 * consumed here exactly once — when a field is added to {@link PlayCardRequest}, this is the
 * only place that must learn about it. (Two hand-maintained copies of this dispatch previously
 * drifted apart twice: an AI chain missing {@code discardHandCardIndex}, then a flashback
 * branch missing tap/retrace payments.)
 *
 * <p>Validation failures surface as {@link IllegalArgumentException}/{@link IllegalStateException}
 * from the engine; callers decide whether to report them to a connection (backend) or swallow
 * them as a no-op (AI).
 */
@Service
@RequiredArgsConstructor
public class PlayCardRequestDispatchService {

    private final GameService gameService;

    public void dispatch(GameData gameData, Player player, PlayCardRequest request) {
        if (Boolean.TRUE.equals(request.fromLibraryTop())) {
            gameService.playCardFromLibraryTop(gameData, player, request.xValue(), request.targetId());
            return;
        }
        if (Boolean.TRUE.equals(request.flashback())) {
            CardType chosenGraveyardType = request.chosenGraveyardType() != null
                    ? CardType.valueOf(request.chosenGraveyardType()) : null;
            if (!listOrEmpty(request.beholdPermanentIds()).isEmpty()
                    || !listOrEmpty(request.beholdHandCardIndices()).isEmpty()) {
                if (listOrEmpty(request.additionalCostSacrificePermanentIds()).isEmpty()) {
                    gameService.playFlashbackSpell(gameData, player, request.cardIndex(), request.xValue(), request.targetId(),
                            listOrEmpty(request.targetIds()), request.exileGraveyardCardIndices(), chosenGraveyardType,
                            listOrEmpty(request.alternateCostSacrificePermanentIds()), request.discardHandCardIndex(),
                            request.sacrificePermanentId(), listOrEmpty(request.beholdPermanentIds()),
                            listOrEmpty(request.beholdHandCardIndices()));
                    return;
                }
                gameService.playFlashbackSpell(gameData, player, request.cardIndex(), request.xValue(), request.targetId(),
                        listOrEmpty(request.targetIds()), request.exileGraveyardCardIndices(), chosenGraveyardType,
                        listOrEmpty(request.alternateCostSacrificePermanentIds()), request.discardHandCardIndex(),
                        request.sacrificePermanentId(), listOrEmpty(request.additionalCostSacrificePermanentIds()),
                        listOrEmpty(request.beholdPermanentIds()),
                        listOrEmpty(request.beholdHandCardIndices()));
                return;
            }
            if (listOrEmpty(request.additionalCostSacrificePermanentIds()).isEmpty()) {
                gameService.playFlashbackSpell(gameData, player, request.cardIndex(), request.xValue(), request.targetId(),
                        listOrEmpty(request.targetIds()), request.exileGraveyardCardIndices(), chosenGraveyardType,
                        listOrEmpty(request.alternateCostSacrificePermanentIds()), request.discardHandCardIndex(),
                        request.sacrificePermanentId(), request.damageAssignments());
            } else {
                gameService.playFlashbackSpell(gameData, player, request.cardIndex(), request.xValue(), request.targetId(),
                        listOrEmpty(request.targetIds()), request.exileGraveyardCardIndices(), chosenGraveyardType,
                        listOrEmpty(request.alternateCostSacrificePermanentIds()), request.discardHandCardIndex(),
                        request.sacrificePermanentId(), listOrEmpty(request.additionalCostSacrificePermanentIds()),
                        request.damageAssignments());
            }
            return;
        }
        if (request.fromExileCardId() != null) {
            gameService.playCardFromExile(gameData, player, request.fromExileCardId(), request.xValue(),
                    request.targetId(), listOrEmpty(request.exileCounterCostPermanentIds()));
            return;
        }
        if (Boolean.TRUE.equals(request.morph())) {
            gameService.playCardWithMorph(gameData, player, request.cardIndex(), request.xValue(), request.targetId(),
                    request.damageAssignments(), listOrEmpty(request.targetIds()), request.discardHandCardIndex());
            return;
        }
        CardSubtype chosenBeholdType = request.beholdCreatureType() != null
                ? CardSubtype.valueOf(request.beholdCreatureType()) : null;
        CardSubtype chosenCreatureType = request.chosenCreatureType() != null
                ? CardSubtype.valueOf(request.chosenCreatureType()) : null;
        // The empty-to-null normalization on the two list costs mirrors the presence checks the
        // former per-field branches keyed on, so an empty list still means "cost not used".
        if (request.sharedColorDiscardHandCardIndex() != null) {
            gameService.playCard(gameData, player, request.cardIndex(), request.xValue(), request.targetId(),
                    request.damageAssignments(),
                    listOrEmpty(request.targetIds()), listOrEmpty(request.convokeCreatureIds()),
                    Boolean.TRUE.equals(request.fromGraveyard()), request.sacrificePermanentId(),
                    request.phyrexianLifeCount(), nullIfEmpty(request.alternateCostSacrificePermanentIds()),
                    request.exileGraveyardCardIndex(), nullIfEmpty(request.exileGraveyardCardIndices()),
                    Boolean.TRUE.equals(request.kicked()), request.discardHandCardIndex(),
                    nullIfEmpty(request.discardHandCardIndices()),
                    nullIfEmpty(request.imposedSacrificePermanentIds()),
                    nullIfEmpty(request.additionalCostSacrificePermanentIds()),
                    request.repeatedAdditionalCosts() != null ? request.repeatedAdditionalCosts() : List.of(),
                    Boolean.TRUE.equals(request.buyback()), request.sharedColorDiscardHandCardIndex());
            return;
        }
        if (chosenCreatureType == null) {
            gameService.playCard(gameData, player, request.cardIndex(), request.xValue(), request.targetId(),
                    request.damageAssignments(),
                    listOrEmpty(request.targetIds()), listOrEmpty(request.convokeCreatureIds()),
                    Boolean.TRUE.equals(request.fromGraveyard()), request.sacrificePermanentId(),
                    request.phyrexianLifeCount(), nullIfEmpty(request.alternateCostSacrificePermanentIds()),
                    request.exileGraveyardCardIndex(), nullIfEmpty(request.exileGraveyardCardIndices()),
                    Boolean.TRUE.equals(request.kicked()), request.discardHandCardIndex(),
                    nullIfEmpty(request.discardHandCardIndices()),
                    nullIfEmpty(request.imposedSacrificePermanentIds()),
                    nullIfEmpty(request.additionalCostSacrificePermanentIds()),
                    request.repeatedAdditionalCosts() != null ? request.repeatedAdditionalCosts() : List.of(),
                    Boolean.TRUE.equals(request.buyback()),
                    request.beholdPermanentId(), request.beholdHandCardIndex(),
                    listOrEmpty(request.beholdPermanentIds()), listOrEmpty(request.beholdHandCardIndices()),
                    chosenBeholdType);
            return;
        }
        gameService.playCard(gameData, player, request.cardIndex(), request.xValue(), request.targetId(),
                request.damageAssignments(), listOrEmpty(request.targetIds()), listOrEmpty(request.convokeCreatureIds()),
                Boolean.TRUE.equals(request.fromGraveyard()), request.sacrificePermanentId(),
                request.phyrexianLifeCount(), nullIfEmpty(request.alternateCostSacrificePermanentIds()),
                request.exileGraveyardCardIndex(), nullIfEmpty(request.exileGraveyardCardIndices()),
                Boolean.TRUE.equals(request.kicked()), request.discardHandCardIndex(),
                nullIfEmpty(request.discardHandCardIndices()), nullIfEmpty(request.imposedSacrificePermanentIds()),
                nullIfEmpty(request.additionalCostSacrificePermanentIds()),
                request.repeatedAdditionalCosts() != null ? request.repeatedAdditionalCosts() : List.of(),
                Boolean.TRUE.equals(request.buyback()), request.beholdPermanentId(), request.beholdHandCardIndex(),
                listOrEmpty(request.beholdPermanentIds()), listOrEmpty(request.beholdHandCardIndices()),
                chosenBeholdType, chosenCreatureType);
    }

    private static <T> List<T> listOrEmpty(List<T> list) {
        return list != null ? list : List.of();
    }

    private static <T> List<T> nullIfEmpty(List<T> list) {
        return list == null || list.isEmpty() ? null : list;
    }
}
