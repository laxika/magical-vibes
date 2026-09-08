package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnPermanentTypeToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificeUnlessReturnPermanentTypeToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

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
        List<Permanent> validPermanents = findValidPermanents(gameData, e, sourceCard);

        if (validPermanents.isEmpty()) {
            if (sourcePermanent != null) {
                permanentRemovalService.removePermanentToGraveyard(gameData, sourcePermanent);
                gameLogService.append(gameData, GameLog.cardThen(sourceCard, " is sacrificed because there is no "
                        + e.permanentType().name().toLowerCase() + " to return."));
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

    public static List<Permanent> findValidPermanents(GameData gameData,
                                                       SacrificeUnlessReturnPermanentTypeToHandEffect effect,
                                                       Card sourceCard) {
        List<Permanent> validPermanents = new ArrayList<>();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().hasType(effect.permanentType())) {
                    validPermanents.add(permanent);
                }
            }
        }
        return validPermanents;
    }
}
