package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeEverflameEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BecomeEverflameEffectHandler implements NormalEffectHandlerBean {

    private static final String EVERFLAME_NAME = "Everflame, Heroes' Legacy";

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeEverflameEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null || EVERFLAME_NAME.equals(source.getPersistentName())) {
            return;
        }

        source.setPersistentName(EVERFLAME_NAME);
        if (!source.getGrantedSubtypes().contains(CardSubtype.EQUIPMENT)) {
            source.getGrantedSubtypes().add(CardSubtype.EQUIPMENT);
        }
        source.setLosesAllAbilitiesPermanently(true);

        addFloatingEffect(gameData, entry, source,
                new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.PERMANENT));
        addFloatingEffect(gameData, entry, source,
                new GrantActivatedAbilityEffect(new EquipActivatedAbility("{3}"), GrantScope.TARGET,
                        null, EffectDuration.PERMANENT));
        addFloatingEffect(gameData, entry, source,
                new GrantEffectEffect(new StaticBoostEffect(3, 3, GrantScope.EQUIPPED_CREATURE),
                        GrantScope.TARGET));

        gameLogService.append(gameData, GameLog.builder().card(source.getCard())
                .text(" becomes " + EVERFLAME_NAME + ".").build());
        log.info("Game {} - {} becomes {}", gameData.id, source.getCard().getName(), EVERFLAME_NAME);
    }

    private void addFloatingEffect(GameData gameData, StackEntry entry, Permanent source,
                                   CardEffect effect) {
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), source.getId(), entry.getControllerId(),
                effect, source.getId(), null, null, EffectDuration.PERMANENT, 0));
    }
}
