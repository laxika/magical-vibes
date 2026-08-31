package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles one player's choice to sacrifice creatures in exchange for sacrificing the source. */
@Component
@RequiredArgsConstructor
public class AnyPlayerMaySacrificeCreaturesThenSacrificeSourceHandler implements MayEffectHandlerBean {

    private final AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffectHandler effectHandler;
    private final EffectResolutionService effectResolutionService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect) ability.effects().getFirst();
        UUID chooserId = ability.controllerId();
        List<UUID> remaining = effectHandler.remainingAfter(gameData, effect, chooserId);

        if (accepted && effectHandler.creatureIds(gameData, chooserId, effect.abilityControllerId()).size()
                >= effect.count()) {
            List<CardEffect> effects = new ArrayList<>();
            effects.add(new SacrificePermanentsEffect(
                    effect.count(),
                    new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                    SacrificeRecipient.TARGET_PLAYER));
            effects.add(new SacrificeSelfEffect());
            if (!remaining.isEmpty()) {
                effects.add(new AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect(
                        effect.count(), remaining, effect.abilityControllerId(), effect.sourcePermanentId()));
            }
            effectResolutionService.resolveEffects(gameData, new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    effect.abilityControllerId(),
                    ability.sourceCard().getName() + "'s ability",
                    effects,
                    chooserId,
                    effect.sourcePermanentId()));
        } else if (!remaining.isEmpty()) {
            effectHandler.promptNext(gameData, ability.sourceCard(),
                    new AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect(
                            effect.count(), remaining, effect.abilityControllerId(), effect.sourcePermanentId()));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
