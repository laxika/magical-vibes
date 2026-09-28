package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndMayCastRandomSideboardCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromSideboardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class ExileTargetCreatureAndMayCastRandomSideboardCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureAndMayCastRandomSideboardCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null || !permanentRemovalService.removePermanentToExile(gameData, target)) {
            return;
        }

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is exiled."));
        permanentRemovalService.removeOrphanedAuras(gameData);

        List<Card> nonlandCards = gameData.playerSideboards
                .getOrDefault(targetControllerId, List.of())
                .stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .toList();
        if (nonlandCards.isEmpty()) {
            return;
        }

        Card chosenCard = nonlandCards.get(ThreadLocalRandom.current().nextInt(nonlandCards.size()));
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                chosenCard,
                targetControllerId,
                List.of(new MayCastFromSideboardWithoutPayingManaCostEffect(targetControllerId)),
                "Cast " + chosenCard.getName() + " without paying its mana cost?"
        ));
    }
}
