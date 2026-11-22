package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
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
        if (staticBonus.losesAllAbilities() || target.isFaceDown()) {
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        } else {
            abilities.addAll(target.getCard().getActivatedAbilities(Zone.BATTLEFIELD));
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        }
        abilities.addAll(gameQueryService.missingIntrinsicManaAbilities(gameData, target, abilities));
        // The abilities are those the creature has as this resolves (CR 611.2c).
        abilities.forEach(ability ->
                gameData.grantAbilityToPermanent(entry, ability, source.getId(), Duration.UNTIL_END_OF_TURN));

        gameLogService.append(gameData, GameLog.builder().card(source.getCard())
                .text(" gains the activated abilities of " + FaceDownVisibility.publicName(gameData, target) + " until end of turn.")
                .build());
    }
}
