package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbrightFlamekinEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the conditional half of Soulbright Flamekin's {@code {2}} ability: if this is the third
 * time the ability has resolved this turn, the controller adds {@code {R}{R}{R}{R}{R}{R}{R}{R}}. The
 * unconditional "target creature gains trample" grant is a separate {@code GrantKeywordEffect}.
 *
 * <p>Resolutions are counted per source permanent in {@link GameData#permanentAbilityResolutionsThisTurn}
 * (reset each turn), so the mana is produced on the exact third resolution and not on any later one.
 */
@Component
@RequiredArgsConstructor
public class SoulbrightFlamekinEffectHandler implements NormalEffectHandlerBean {

    private static final int MANA_RESOLUTION = 3;
    private static final int MANA_AMOUNT = 8;

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SoulbrightFlamekinEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        if (selfId == null) {
            return;
        }

        int resolutions = gameData.permanentAbilityResolutionsThisTurn.merge(selfId, 1, Integer::sum);
        if (resolutions != MANA_RESOLUTION) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        pool.add(ManaColor.RED, MANA_AMOUNT);
        Permanent source = gameQueryService.findPermanentById(gameData, selfId);
        if (source != null && gameQueryService.isCreature(gameData, source)) {
            pool.addCreatureMana(ManaColor.RED, MANA_AMOUNT);
        }

        gameBroadcastService.logAndBroadcast(gameData,
                entry.getCard().getName() + " adds " + MANA_AMOUNT + " " + ManaColor.RED.getCode() + ".");
    }
}
