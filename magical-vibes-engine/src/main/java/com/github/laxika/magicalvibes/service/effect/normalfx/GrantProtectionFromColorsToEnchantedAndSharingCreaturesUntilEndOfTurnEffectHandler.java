package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorsToEnchantedAndSharingCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GrantProtectionFromColorsToEnchantedAndSharingCreaturesUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromColorsToEnchantedAndSharingCreaturesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantProtectionFromColorsToEnchantedAndSharingCreaturesUntilEndOfTurnEffect) effect;
        Permanent currentEnchanted = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (currentEnchanted == null) {
            var lastKnown = entry.lastKnownPermanentCard(entry.getTargetId());
            if (lastKnown == null || !lastKnown.hasType(CardType.CREATURE)) {
                return;
            }
            currentEnchanted = new Permanent(lastKnown);
        } else if (!gameQueryService.isCreature(gameData, currentEnchanted)) {
            return;
        }
        Permanent enchanted = currentEnchanted;

        int[] count = {0};
        gameData.forEachPermanent((ignored, permanent) -> {
            if (!gameQueryService.isCreature(gameData, permanent)
                    || (!permanent.getId().equals(enchanted.getId())
                    && !gameQueryService.shareCreatureType(gameData, enchanted, permanent))) {
                return;
            }

            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                    new ProtectionFromColorsEffect(grant.colors(), GrantScope.TARGET),
                    permanent.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
            count[0]++;
        });

        String colors = grant.colors().stream()
                .map(CardColor::name)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" and "));
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" gives " + count[0] + " creature(s) protection from " + colors
                        + " until end of turn.")
                .build());
    }
}
