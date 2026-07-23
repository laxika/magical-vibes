package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.CardType;
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
            gameService.playFlashbackSpell(gameData, player, request.cardIndex(), request.xValue(), request.targetId(),
                    listOrEmpty(request.targetIds()), request.exileGraveyardCardIndices(), chosenGraveyardType,
                    listOrEmpty(request.alternateCostSacrificePermanentIds()), request.discardHandCardIndex());
            return;
        }
        if (request.fromExileCardId() != null) {
            gameService.playCardFromExile(gameData, player, request.fromExileCardId(), request.xValue(), request.targetId());
            return;
        }
        // The empty-to-null normalization on the two list costs mirrors the presence checks the
        // former per-field branches keyed on, so an empty list still means "cost not used".
        gameService.playCard(gameData, player, request.cardIndex(), request.xValue(), request.targetId(),
                request.damageAssignments(),
                listOrEmpty(request.targetIds()), listOrEmpty(request.convokeCreatureIds()),
                Boolean.TRUE.equals(request.fromGraveyard()), request.sacrificePermanentId(),
                request.phyrexianLifeCount(), nullIfEmpty(request.alternateCostSacrificePermanentIds()),
                request.exileGraveyardCardIndex(), nullIfEmpty(request.exileGraveyardCardIndices()),
                Boolean.TRUE.equals(request.kicked()), request.discardHandCardIndex(),
                nullIfEmpty(request.discardHandCardIndices()),
                nullIfEmpty(request.imposedSacrificePermanentIds()));
    }

    private static List<UUID> listOrEmpty(List<UUID> list) {
        return list != null ? list : List.of();
    }

    private static <T> List<T> nullIfEmpty(List<T> list) {
        return list == null || list.isEmpty() ? null : list;
    }
}
