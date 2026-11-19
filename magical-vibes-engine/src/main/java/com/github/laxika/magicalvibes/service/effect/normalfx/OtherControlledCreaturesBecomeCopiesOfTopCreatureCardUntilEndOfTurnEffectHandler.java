package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.OtherControlledCreaturesBecomeCopiesOfTopCreatureCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtherControlledCreaturesBecomeCopiesOfTopCreatureCardUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OtherControlledCreaturesBecomeCopiesOfTopCreatureCardUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null || library.isEmpty()) {
            return;
        }

        Card topCard = library.getFirst();
        if (!topCard.hasType(CardType.CREATURE)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        UUID sourcePermanentId = entry.getSourcePermanentId();
        List<Permanent> creatures = new ArrayList<>();
        for (Permanent permanent : List.copyOf(battlefield)) {
            if (!permanent.getId().equals(sourcePermanentId)
                    && gameQueryService.isCreature(gameData, permanent)) {
                creatures.add(permanent);
            }
        }

        for (Permanent creature : creatures) {
            Card preCopyCard = PermanentCopierService.preCopyCard(gameData, creature);
            permanentCopierService.applyCloneCopy(creature, topCard, null, null, Set.of());
            PermanentCopierService.beginCopy(gameData, entry.getCard().getName(), creature.getId(), entry.getControllerId(),
                    creature, preCopyCard, Duration.UNTIL_END_OF_TURN);
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" makes " + creatures.size() + " other creature(s) a copy of "
                        + topCard.getName() + " until end of turn.")
                .build());
        log.info("Game {} - {} copies {} onto {} creatures until end of turn",
                gameData.id, entry.getCard().getName(), topCard.getName(), creatures.size());
    }
}
