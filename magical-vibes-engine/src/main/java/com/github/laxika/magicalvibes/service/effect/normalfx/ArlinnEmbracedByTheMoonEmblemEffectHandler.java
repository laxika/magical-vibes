package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.ArlinnEmbracedByTheMoonEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArlinnEmbracedByTheMoonEmblemEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ArlinnEmbracedByTheMoonEmblemEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        ActivatedAbility tapAbility = new ActivatedAbility(
                true, null,
                List.of(new DealDamageToAnyTargetEffect(new SourcePower())),
                "{T}: This creature deals damage equal to its power to any target."
        );

        Emblem emblem = new Emblem(controllerId, List.of(
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.OWN_PERMANENTS,
                        new PermanentIsCreaturePredicate()),
                new GrantActivatedAbilityEffect(tapAbility, GrantScope.OWN_PERMANENTS,
                        new PermanentIsCreaturePredicate())
        ), entry.getCard());

        gameData.emblems.add(emblem);

        String logEntry = playerName + " gets an emblem with \"Creatures you control have haste and "
                + "'{T}: This creature deals damage equal to its power to any target.'\".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        log.info("Game {} - {} gets Arlinn, Embraced by the Moon emblem", gameData.id, playerName);
    }
}
