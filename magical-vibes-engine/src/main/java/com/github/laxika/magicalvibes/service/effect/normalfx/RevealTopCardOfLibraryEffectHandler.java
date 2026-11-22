package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Reveals the top card of the library of the player {@link SubjectResolutionService} finds for the effect's subject
 * (the controller's own when it names nobody), with Prophecy's land life-gain rider.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardOfLibraryEffect revealEffect = (RevealTopCardOfLibraryEffect) effect;

        List<UUID> playerIds = subjectResolutionService.players(gameData, entry, effect, revealEffect.player());
        if (playerIds.isEmpty() && !(revealEffect.player() instanceof PlayerSubject.Each)) {
            playerIds = List.of(entry.getControllerId());
        }
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext context = AmountContext.forStackEntry(entry, source);
        List<UUID> revealedIds = new ArrayList<>();
        List<UUID> revealingPlayerIds = new ArrayList<>();
        List<MovedCard> whereTheyAre = new ArrayList<>();
        int landsRevealed = 0;
        for (UUID playerId : playerIds) {
            List<Card> library = gameData.playerDecks.getOrDefault(playerId, List.of());
            String playerName = gameData.playerIdToName.get(playerId);
            int count = amountEvaluationService.evaluate(
                    gameData, revealEffect.count(), context.withControllerId(playerId));
            if (count <= 0) {
                continue;
            }
            if (library.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty."));
                continue;
            }
            List<Card> revealed = List.copyOf(library.subList(0, Math.min(count, library.size())));
            revealingPlayerIds.add(playerId);
            GameLog.Builder reveal = GameLog.builder().text(playerName + " reveals ");
            for (int i = 0; i < revealed.size(); i++) {
                Card card = revealed.get(i);
                revealedIds.add(card.getId());
                whereTheyAre.add(new MovedCard(card.getId(), card.getId(), Zone.LIBRARY, playerId));
                if (card.hasType(CardType.LAND)) {
                    landsRevealed++;
                }
                if (i > 0) {
                    reveal.text(", ");
                }
                reveal.card(card);
            }
            gameLogService.append(gameData, reveal.text(" from the top of their library.").build());
        }
        entry.recordStep(StepKind.REVEAL, StepOutcome.ofObjects(revealedIds, revealingPlayerIds, whereTheyAre));
        if (revealEffect.lifeGainIfLand() > 0 && landsRevealed > 0) {
            lifeSupport.applyGainLife(gameData, entry.getControllerId(), revealEffect.lifeGainIfLand() * landsRevealed,
                    entry.getCard().getName(), entry.getCard(), entry.getEntryType());
        }
    }
}
