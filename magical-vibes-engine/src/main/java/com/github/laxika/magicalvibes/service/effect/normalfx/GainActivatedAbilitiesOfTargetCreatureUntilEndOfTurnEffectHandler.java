package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GainActivatedAbilitiesOfTargetCreatureUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAbilitiesOfTargetCreatureUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null) {
            return;
        }

        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, target);
        List<ActivatedAbility> abilities = new ArrayList<>();
        if (staticBonus.losesAllAbilities() || target.isLosesAllAbilitiesUntilEndOfTurn()) {
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        } else {
            abilities.addAll(target.getCard().getActivatedAbilities());
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        }
        abilities.addAll(target.getPersistentGrantedActivatedAbilities());
        abilities.addAll(target.getTemporaryActivatedAbilities());
        abilities.addAll(target.getUntilNextTurnActivatedAbilities());
        source.getTemporaryActivatedAbilities().addAll(abilities);

        gameLogService.append(gameData, GameLog.builder().card(source.getCard())
                .text(" gains the activated abilities of " + target.getCard().getName() + " until end of turn.")
                .build());
    }
}
