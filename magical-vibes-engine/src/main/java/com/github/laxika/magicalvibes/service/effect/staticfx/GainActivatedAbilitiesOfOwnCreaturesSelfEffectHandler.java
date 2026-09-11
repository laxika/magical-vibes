package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfOwnCreaturesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GainActivatedAbilitiesOfOwnCreaturesSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAbilitiesOfOwnCreaturesEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect,
                      StaticBonusAccumulator accumulator) {
        GainActivatedAbilitiesOfOwnCreaturesEffect gainEffect =
                (GainActivatedAbilitiesOfOwnCreaturesEffect) effect;
        GameData gameData = context.gameData();
        if (context.sourceControllerId() == null) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(context.sourceControllerId());
        if (battlefield == null) return;

        boolean hasAnimateArtifacts = support.hasAnimateArtifactEffect(gameData);
        for (Permanent permanent : battlefield) {
            if ((!support.isEffectivelyCreature(gameData, permanent, hasAnimateArtifacts)
                    && !gameQueryService.isCreature(gameData, permanent))
                    || !support.matchesStaticFilter(context, permanent, gainEffect.filter())) {
                continue;
            }
            addEffectiveActivatedAbilities(gameData, permanent, accumulator);
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
        if (!losesAllAbilities && !permanent.isFaceDown()) {
            List<CardEffect> onTapEffects = permanent.getCard().getEffects(EffectSlot.ON_TAP);
            if (!onTapEffects.isEmpty()) {
                abilities.add(new ActivatedAbility(true, null, onTapEffects, "{T}: Add mana."));
            }
        }
        abilities.forEach(accumulator::addActivatedAbility);
    }
}
