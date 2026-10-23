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
import com.github.laxika.magicalvibes.service.effect.normalfx.SacrificePermanentsEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Handles one player's choice to sacrifice creatures in exchange for sacrificing the source (Prowling
 * Pangolin). An accepting player who controls exactly the required number of creatures sacrifices
 * them at once, and nothing changes before that, so a creature with competing death replacements is
 * asked about before it leaves (CR 616.1) and the answer is completed again with that choice. One
 * who controls more opens the already covered forced-sacrifice choice.
 */
@Component
@RequiredArgsConstructor
public class AnyPlayerMaySacrificeCreaturesThenSacrificeSourceHandler implements MayEffectHandlerBean {

    private final AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffectHandler effectHandler;
    private final EffectResolutionService effectResolutionService;
    private final InputCompletionService inputCompletionService;
    private final SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = (AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect) ability.effects().getFirst();
        UUID chooserId = ability.controllerId();
        List<UUID> remaining = effectHandler.remainingAfter(gameData, effect, chooserId);
        int creatureCount = accepted
                ? effectHandler.creatureIds(gameData, chooserId, effect.abilityControllerId()).size()
                : 0;

        if (accepted && creatureCount == effect.count()) {
            permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                    gameData, player, ability, true,
                    () -> sacrificeEveryCreatureThenSource(gameData, ability, effect, chooserId, remaining));
            return;
        }
        if (accepted && creatureCount > effect.count()) {
            effectResolutionService.resolveEffects(gameData, sacrificeEntry(ability, effect, chooserId, remaining));
        } else if (!remaining.isEmpty()) {
            effectHandler.promptNext(gameData, ability.sourceCard(),
                    new AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect(
                            effect.count(), remaining, effect.abilityControllerId(), effect.sourcePermanentId()));
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Sacrifices every creature of a chooser who controls exactly the required number, then the
     * source, then offers the next player the choice. Nothing changes before those creatures are
     * sacrificed together, so their competing death replacements are asked about first (CR 616.1).
     */
    private void sacrificeEveryCreatureThenSource(GameData gameData, PendingMayAbility ability,
                                                  AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect effect,
                                                  UUID chooserId, List<UUID> remaining) {
        StackEntry entry = sacrificeEntry(ability, effect, chooserId, remaining);
        sacrificePermanentsEffectHandler.resolve(gameData, entry, entry.getEffectsToResolve().getFirst());
        gameData.deathReplacementsCanRerunEffect = false;
        effectResolutionService.resolveEffectsFrom(gameData, entry, 1);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private StackEntry sacrificeEntry(PendingMayAbility ability,
                                      AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect effect,
                                      UUID chooserId, List<UUID> remaining) {
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
        return new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                ability.sourceCard(),
                effect.abilityControllerId(),
                ability.sourceCard().getName() + "'s ability",
                effects,
                chooserId,
                effect.sourcePermanentId());
    }
}
