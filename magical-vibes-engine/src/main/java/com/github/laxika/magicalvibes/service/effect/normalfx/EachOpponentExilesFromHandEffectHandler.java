package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentExilesFromHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Nicol Bolas, God-Pharaoh +1: each opponent exiles N cards from their hand (their choice); with another player subject
 * each player (Mind Swords) or you (Ego Drain). The players are asked in APNAP order (CR 101.4); a player with an
 * empty hand is skipped.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachOpponentExilesFromHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentExilesFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachOpponentExilesFromHandEffect) effect;
        // A "until this leaves the battlefield" exile never happens if the source already left.
        if (e.returnOnSourceLeave() && (entry.getSourcePermanentId() == null
                || gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) == null)) {
            return;
        }
        UUID controllerId = entry.getControllerId();
        List<UUID> players = subjectResolutionService.players(gameData, entry, e, e.players());

        List<UUID> choosers = new ArrayList<>();
        for (UUID playerId : players) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                String name = gameData.playerIdToName.get(playerId);
                gameLogService.append(gameData,
                        GameLog.text(name + " has no cards to exile from hand."));
                continue;
            }
            choosers.add(playerId);
        }

        if (choosers.isEmpty()) {
            return;
        }

        UUID first = choosers.getFirst();
        List<UUID> remaining = choosers.size() > 1 ? List.copyOf(choosers.subList(1, choosers.size())) : List.of();
        if (e.grantPlayPermissionToChooser()) {
            playerInputService.beginExileFromHandChoice(gameData, first, null, null, e.amount(),
                    remaining, e.amount(), false, false, null, true, controllerId,
                    e.exilePlayOpponentTax(), e.landsEnterTapped());
        } else {
            playerInputService.beginExileFromHandChoice(gameData, first, entry.getSourcePermanentId(),
                    null, e.amount(), remaining, e.amount(), false, e.returnOnSourceLeave());
        }
    }
}
