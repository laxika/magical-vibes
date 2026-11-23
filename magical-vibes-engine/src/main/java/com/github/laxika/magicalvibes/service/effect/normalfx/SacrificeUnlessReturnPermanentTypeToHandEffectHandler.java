package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeUnlessReturnPermanentTypeToHandEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final StepDepartureSupport stepDepartureSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeUnlessReturnPermanentTypeToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeUnlessReturnPermanentTypeToHandEffect) effect;
        UUID controllerId = entry.getControllerId();
        Card sourceCard = entry.getCard();
        Permanent sourcePermanent = findSourcePermanent(gameData, controllerId, sourceCard);
        List<Permanent> validPermanents = findValidPermanents(gameData, gameQueryService, e);

        if (validPermanents.isEmpty()) {
            if (sourcePermanent != null) {
                gameLogService.append(gameData, GameLog.text("There is no "
                        + e.permanentType().name().toLowerCase() + " to return."));
                stepDepartureSupport.sacrifice(gameData, entry, effect, sourcePermanent.getId());
                log.info("Game {} - {} sacrificed (no {}s to return)", gameData.id, sourceCard.getName(),
                        e.permanentType().name().toLowerCase());
            }
            return;
        }

        String typeName = e.permanentType().name().toLowerCase();
        String prompt = sourcePermanent != null
                ? "Return an " + typeName + " to its owner's hand? If you don't, " + sourceCard.getName()
                + " will be sacrificed."
                : sourceCard.getName() + " is no longer on the battlefield. Return an " + typeName
                + " to its owner's hand anyway?";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(sourceCard, controllerId, List.of(e), prompt));
    }

    private static Permanent findSourcePermanent(GameData gameData, UUID controllerId, Card sourceCard) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return null;
        }
        return battlefield.stream()
                .filter(permanent -> permanent.getCard().getId().equals(sourceCard.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * The permanents of the effect's type, judged by their current types (CR 613): a face-down permanent has only its
     * face-down types (CR 708.2a), and a type an effect grants counts.
     */
    public static List<Permanent> findValidPermanents(GameData gameData, GameQueryService gameQueryService,
                                                       SacrificeUnlessReturnPermanentTypeToHandEffect effect) {
        List<Permanent> validPermanents = new ArrayList<>();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.hasEffectiveCardType(gameData, permanent, effect.permanentType())) {
                    validPermanents.add(permanent);
                }
            }
        }
        return validPermanents;
    }
}
