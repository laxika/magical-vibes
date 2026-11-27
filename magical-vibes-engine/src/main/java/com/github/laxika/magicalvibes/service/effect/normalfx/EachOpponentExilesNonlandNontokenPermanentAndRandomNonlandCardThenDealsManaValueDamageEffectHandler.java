package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentExilesNonlandNontokenPermanentAndRandomNonlandCardThenDealsManaValueDamageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Gilded Ambusher's reflexive exile and damage once every opponent has chosen their permanent: the chosen
 * permanents still controlled by an opponent are exiled together as one event, then a random nonland card from each
 * opponent's library, then each opponent is dealt the total mana value of their own exiled cards, each exiled
 * permanent counted by its card (not a copy it was).
 */
@Component
@RequiredArgsConstructor
public class EachOpponentExilesNonlandNontokenPermanentAndRandomNonlandCardThenDealsManaValueDamageEffectHandler
        implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentExilesNonlandNontokenPermanentAndRandomNonlandCardThenDealsManaValueDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var ambusherEffect =
                (EachOpponentExilesNonlandNontokenPermanentAndRandomNonlandCardThenDealsManaValueDamageEffect) effect;
        List<UUID> opponents = SubjectResolutionService.apnapOpponents(gameData, entry.getControllerId()).stream()
                .filter(gameData.playerIds::contains)
                .toList();
        List<Permanent> chosen = subjectResolutionService.permanents(gameData, entry, effect, ambusherEffect.chosen());
        Map<UUID, Integer> exiledManaValue = new LinkedHashMap<>();
        opponents.forEach(opponentId -> exiledManaValue.put(opponentId, 0));

        String sourceName = entry.getCard().getName();
        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            for (Permanent permanent : chosen) {
                UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
                if (controllerId != null && exiledManaValue.containsKey(controllerId)) {
                    // The exiled card is the card that was on the battlefield, not the copy it was impersonating.
                    exiledManaValue.merge(controllerId, permanent.getOriginalCard().getManaValue(), Integer::sum);
                    exileSupport.exilePermanentAndLog(gameData, permanent, sourceName);
                }
            }
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }

        for (UUID opponentId : opponents) {
            Card randomNonland = exileRandomNonlandCard(gameData, opponentId);
            if (randomNonland != null) {
                exiledManaValue.merge(opponentId, randomNonland.getManaValue(), Integer::sum);
                gameLogService.append(gameData, GameLog.cardThen(randomNonland,
                        " is exiled at random from " + gameData.playerIdToName.get(opponentId)
                                + "'s library by " + sourceName + "."));
            }
        }

        exiledManaValue.forEach((opponentId, damage) -> {
            if (damage > 0) {
                damageSupport.dealDamageToPlayer(gameData, entry, opponentId, damage);
            }
        });
        gameOutcomeService.checkWinCondition(gameData);
    }

    private Card exileRandomNonlandCard(GameData gameData, UUID playerId) {
        List<Card> library = gameData.playerDecks.getOrDefault(playerId, List.of());
        List<Card> nonlands = library.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .toList();
        if (nonlands.isEmpty()) {
            return null;
        }

        Card chosen = nonlands.get(ThreadLocalRandom.current().nextInt(nonlands.size()));
        if (!library.removeIf(card -> card.getId().equals(chosen.getId()))) {
            return null;
        }
        exileService.exileCard(gameData, playerId, chosen);
        return chosen;
    }
}
