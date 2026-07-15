package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChampionCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChampionCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChampionCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ChampionCreatureEffect) effect;

        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        String playerName = gameData.playerIdToName.get(controllerId);

        Permanent sourcePermanent = findSourcePermanent(gameData, controllerId, sourceCard);
        List<UUID> validIds = collectValidChampionTargets(gameData, sourcePermanent, controllerId, e.championedSubtypes());

        if (validIds.isEmpty()) {
            if (sourcePermanent != null) {
                permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
                String logEntry = playerName + " controls no other "
                        + championQualityLabel(e.championedSubtypes())
                        + ". " + sourceCard.getName() + " is sacrificed.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} sacrificed (no creature to champion)", gameData.id, sourceCard.getName());
            }
            return;
        }

        if (sourcePermanent == null) {
            log.info("Game {} - {} no longer on battlefield, champion effect is a no-op",
                    gameData.id, sourceCard.getName());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ChampionCreature(sourcePermanent.getId(), controllerId));
        String prompt = "Choose another " + championQualityLabel(e.championedSubtypes())
                + " you control to exile.";
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds, prompt);
    }

    private Permanent findSourcePermanent(GameData gameData, UUID controllerId, Card sourceCard) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return null;
        }
        for (Permanent p : battlefield) {
            if (p.getCard().getId().equals(sourceCard.getId())) {
                return p;
            }
        }
        return null;
    }

    private List<UUID> collectValidChampionTargets(GameData gameData, Permanent sourcePermanent,
                                                   UUID controllerId, List<CardSubtype> championedSubtypes) {
        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || sourcePermanent == null) {
            return validIds;
        }
        for (Permanent p : battlefield) {
            if (isValidChampionTarget(gameData, sourcePermanent, p, championedSubtypes)) {
                validIds.add(p.getId());
            }
        }
        return validIds;
    }

    private boolean isValidChampionTarget(GameData gameData, Permanent source, Permanent candidate,
                                          List<CardSubtype> championedSubtypes) {
        if (candidate.getId().equals(source.getId())) {
            return false;
        }
        if (!gameQueryService.isCreature(gameData, candidate)) {
            return false;
        }
        return championedSubtypes.isEmpty()
                || championedSubtypes.stream()
                        .anyMatch(subtype -> GameQueryService.permanentHasSubtype(candidate, subtype));
    }

    private static String championQualityLabel(List<CardSubtype> championedSubtypes) {
        if (championedSubtypes.isEmpty()) {
            return "creature";
        }
        return championedSubtypes.stream()
                .map(subtype -> subtype.name().toLowerCase())
                .collect(java.util.stream.Collectors.joining(" or "));
    }
}
