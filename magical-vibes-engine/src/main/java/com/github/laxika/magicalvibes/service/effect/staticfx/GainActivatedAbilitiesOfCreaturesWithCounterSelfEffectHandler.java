package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCreaturesWithCounterEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GainActivatedAbilitiesOfCreaturesWithCounterSelfEffectHandler
        implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAbilitiesOfCreaturesWithCounterEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect,
                      StaticBonusAccumulator accumulator) {
        GainActivatedAbilitiesOfCreaturesWithCounterEffect gainEffect =
                (GainActivatedAbilitiesOfCreaturesWithCounterEffect) effect;
        GameData gameData = context.gameData();
        boolean hasAnimateArtifacts = support.hasAnimateArtifactEffect(gameData);

        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getId().equals(context.sourceId())
                        || permanent.getCounterCount(gainEffect.counterType()) == 0
                        || (!support.isEffectivelyCreature(gameData, permanent, hasAnimateArtifacts)
                        && !gameQueryService.isCreature(gameData, permanent))) {
                    continue;
                }
                addEffectiveActivatedAbilities(gameData, permanent, accumulator);
            }
        }
    }

    private void addEffectiveActivatedAbilities(GameData gameData, Permanent permanent,
                                                StaticBonusAccumulator accumulator) {
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, permanent);
        boolean losesAllAbilities = staticBonus.losesAllAbilities()
                || permanent.isLosesAllAbilitiesUntilEndOfTurn();
        List<ActivatedAbility> abilities = new ArrayList<>();
        if (losesAllAbilities || permanent.isFaceDown()) {
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        } else if (staticBonus.losesAllNonManaAbilities()) {
            abilities.addAll(permanent.getCard().getActivatedAbilities(Zone.BATTLEFIELD).stream()
                    .filter(ActivatedAbility::isManaAbility)
                    .toList());
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        } else {
            abilities.addAll(permanent.getCard().getActivatedAbilities(Zone.BATTLEFIELD));
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        }
        abilities.addAll(permanent.getPersistentGrantedActivatedAbilities());
        abilities.addAll(permanent.getTemporaryActivatedAbilities());
        abilities.addAll(permanent.getUntilNextTurnActivatedAbilities());
        abilities.addAll(gameQueryService.missingIntrinsicManaAbilities(gameData, permanent, abilities));
        abilities.forEach(accumulator::addActivatedAbility);
    }
}
