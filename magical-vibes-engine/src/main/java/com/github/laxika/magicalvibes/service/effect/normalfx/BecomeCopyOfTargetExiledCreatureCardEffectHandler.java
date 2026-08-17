package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetExiledCreatureCardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeCopyOfTargetExiledCreatureCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfTargetExiledCreatureCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetZone() != Zone.EXILE || entry.getTargetId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        ExiledCardEntry exiledEntry = gameData.findExiledCard(entry.getTargetId());
        if (source == null || exiledEntry == null
                || exiledEntry.faceDown()
                || !source.getId().equals(exiledEntry.sourcePermanentId())
                || !exiledEntry.card().hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE)) {
            log.info("Game {} - Exiled creature copy target is no longer legal", gameData.id);
            return;
        }

        Card printed = source.getOriginalCard();
        permanentCopierService.applyCloneCopy(source, exiledEntry.card(), 0, 0, java.util.Set.of());
        for (var ability : printed.getActivatedAbilities()) {
            source.getCard().addActivatedAbility(ability);
        }

        gameLogService.append(gameData,
                GameLog.textCardText(printed.getName() + " becomes a copy of ", exiledEntry.card(), "."));
        log.info("Game {} - {} becomes a copy of {}", gameData.id, printed.getName(), exiledEntry.card().getName());
    }
}
