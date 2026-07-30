package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LilianaOfTheDarkRealmsEmblemEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LilianaOfTheDarkRealmsEmblemEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LilianaOfTheDarkRealmsEmblemEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        ActivatedAbility swampAbility = new ActivatedAbility(
                true, null,
                List.of(new AwardManaEffect(ManaColor.BLACK, 4)),
                "{T}: Add {B}{B}{B}{B}."
        );

        Emblem emblem = new Emblem(controllerId, List.of(
                new GrantActivatedAbilityEffect(swampAbility, GrantScope.OWN_PERMANENTS,
                        new PermanentHasSubtypePredicate(CardSubtype.SWAMP))
        ), entry.getCard());

        gameData.emblems.add(emblem);

        String logEntry = playerName + " gets an emblem with \"Swamps you control have '{T}: Add {B}{B}{B}{B}.'\".";
        gameLogService.append(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} gets Liliana of the Dark Realms emblem", gameData.id, playerName);
    }
}
