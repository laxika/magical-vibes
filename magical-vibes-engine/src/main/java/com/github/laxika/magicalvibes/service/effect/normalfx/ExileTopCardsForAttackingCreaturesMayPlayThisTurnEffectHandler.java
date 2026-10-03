package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttackingPermanentSnapshot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileTopCardsMayPlayThisTurnEffectHandler exileHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffect exileEffect =
                (ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffect) effect;
        int totalPower = exileEffect.attackingCreatures().stream()
                .mapToInt(attacker -> powerAtResolution(gameData, attacker))
                .sum();
        exileHandler.resolve(gameData, entry, new ExileTopCardsMayPlayThisTurnEffect(totalPower));
    }

    private int powerAtResolution(GameData gameData, AttackingPermanentSnapshot attacker) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, attacker.permanentId());
        return permanent == null
                ? attacker.powerAtTrigger()
                : gameQueryService.getEffectivePower(gameData, permanent);
    }
}
