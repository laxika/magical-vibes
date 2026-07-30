package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OtherNontokenCreaturesBecomeCopyOfEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtherNontokenCreaturesBecomeCopyOfEnchantedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OtherNontokenCreaturesBecomeCopyOfEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID auraId = entry.getSourcePermanentId();
        if (auraId == null) {
            return;
        }
        Permanent aura = gameQueryService.findPermanentById(gameData, auraId);
        if (aura == null || aura.getAttachedTo() == null) {
            return;
        }
        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted == null) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        // Snapshot first: applying copies must not interleave with iteration over the battlefield.
        List<Permanent> creatures = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (!permanent.getId().equals(enchanted.getId())
                    && !permanent.getCard().isToken()
                    && gameQueryService.isCreature(gameData, permanent)) {
                creatures.add(permanent);
            }
        }

        for (Permanent creature : creatures) {
            permanentCopierService.applyCloneCopy(creature, enchanted, null, null);
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" makes " + creatures.size() + " other nontoken creature(s) a copy of "
                        + enchanted.getCard().getName() + ".")
                .build());
        log.info("Game {} - Infinite Reflection copies {} onto {} creatures",
                gameData.id, enchanted.getCard().getName(), creatures.size());
    }
}
