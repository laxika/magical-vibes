package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfChosenPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GainActivatedAbilitiesOfChosenPermanentSelfEffectHandler implements StaticEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAbilitiesOfChosenPermanentEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        Permanent source = context.source();
        if (source.getChosenPermanentId() == null) return;
        GameData gameData = context.gameData();
        Permanent chosen = gameQueryService.findPermanentById(gameData, source.getChosenPermanentId());
        if (chosen == null) return;

        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, chosen);
        List<ActivatedAbility> abilities = new ArrayList<>();
        if (staticBonus.losesAllAbilities() || chosen.isLosesAllAbilitiesUntilEndOfTurn()) {
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        } else if (staticBonus.losesAllNonManaAbilities()) {
            abilities.addAll(chosen.getCard().getActivatedAbilities(Zone.BATTLEFIELD).stream()
                    .filter(ActivatedAbility::isManaAbility)
                    .toList());
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        } else {
            abilities.addAll(chosen.getCard().getActivatedAbilities(Zone.BATTLEFIELD));
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        }
        abilities.addAll(chosen.getPersistentGrantedActivatedAbilities());
        abilities.addAll(chosen.getTemporaryActivatedAbilities());
        abilities.addAll(chosen.getUntilNextTurnActivatedAbilities());
        abilities.addAll(gameQueryService.missingIntrinsicManaAbilities(gameData, chosen, abilities));
        abilities.stream()
                .filter(ability -> ability.getLoyaltyCost() == null)
                .forEach(accumulator::addActivatedAbility);
    }
}
