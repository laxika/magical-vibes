package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfCreaturesOpponentsControlEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GainActivatedAbilitiesOfCreaturesOpponentsControlSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAbilitiesOfCreaturesOpponentsControlEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        GameData gameData = context.gameData();
        UUID sourceControllerId = context.sourceControllerId();
        if (sourceControllerId == null) return;

        boolean hasAnimateArtifacts = support.hasAnimateArtifactEffect(gameData);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (sourceControllerId.equals(playerId)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (!support.isEffectivelyCreature(gameData, permanent, hasAnimateArtifacts)
                        && !gameQueryService.isCreature(gameData, permanent)) continue;
                addEffectiveActivatedAbilities(gameData, permanent, accumulator);
            }
        }
    }

    private void addEffectiveActivatedAbilities(GameData gameData, Permanent permanent,
                                                StaticBonusAccumulator accumulator) {
        GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, permanent);
        List<ActivatedAbility> abilities = new ArrayList<>();
        if (staticBonus.losesAllAbilities() || permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        } else if (staticBonus.losesAllNonManaAbilities()) {
            abilities.addAll(permanent.getCard().getActivatedAbilities().stream()
                    .filter(ActivatedAbility::isManaAbility)
                    .toList());
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        } else {
            abilities.addAll(permanent.getCard().getActivatedAbilities());
            abilities.addAll(staticBonus.grantedActivatedAbilities());
        }
        abilities.addAll(permanent.getPersistentGrantedActivatedAbilities());
        abilities.addAll(permanent.getTemporaryActivatedAbilities());
        abilities.addAll(permanent.getUntilNextTurnActivatedAbilities());
        if (!staticBonus.losesAllAbilities() && !permanent.isLosesAllAbilitiesUntilEndOfTurn()) {
            List<CardEffect> onTapEffects = permanent.getCard().getEffects(EffectSlot.ON_TAP);
            if (!onTapEffects.isEmpty()) {
                abilities.add(new ActivatedAbility(true, null, onTapEffects, "{T}: Add mana."));
            }
        }
        for (ActivatedAbility ability : abilities) {
            accumulator.addActivatedAbility(ability);
        }
    }
}
