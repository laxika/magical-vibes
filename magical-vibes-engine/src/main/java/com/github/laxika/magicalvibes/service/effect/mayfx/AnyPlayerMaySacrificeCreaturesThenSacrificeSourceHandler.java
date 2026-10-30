package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.effect.normalfx.AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
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
 * Pangolin). An accepting player who controls exactly the required number of creatures has them all
 * chosen; one who controls more picks which. Nothing is sacrificed until every player has answered,
 * then all chosen creatures leave together (CR 616.1 death replacements are asked about first) and
 * the source is sacrificed.
 */
@Component
@RequiredArgsConstructor
public class AnyPlayerMaySacrificeCreaturesThenSacrificeSourceHandler implements MayEffectHandlerBean {

    private final AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffectHandler effectHandler;
    private final EffectResolutionService effectResolutionService;
    private final InputCompletionService inputCompletionService;
    private final DestructionSupport destructionSupport;
    private final PlayerInputService playerInputService;
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
        List<UUID> creatureIds = accepted
                ? effectHandler.creatureIds(gameData, chooserId, effect.abilityControllerId())
                : List.of();

        if (accepted && creatureIds.size() > effect.count()) {
            playerInputService.beginMultiPermanentChoice(gameData, chooserId, creatureIds, effect.count(),
                    new MultiPermanentChoiceContext.AnyPlayerSacrificeCreaturesChoice(
                            ability.sourceCard(), effect, remaining),
                    "Choose " + effect.count() + " creatures to sacrifice.");
            return;
        }
        List<UUID> accepting = accepted && creatureIds.size() == effect.count() ? creatureIds : List.of();
        if (!accepting.isEmpty() && remaining.isEmpty()) {
            permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                    gameData, player, ability, true,
                    () -> sacrificeEverythingChosen(gameData, ability.sourceCard(), effect, accepting));
            return;
        }
        advance(gameData, ability.sourceCard(), effect, accepting, remaining);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Records a player's chosen creatures and offers the choice to the next player, or once everyone has
     * answered sacrifices every chosen creature together and then the source.
     */
    public void completeChoice(GameData gameData, MultiPermanentChoiceContext.AnyPlayerSacrificeCreaturesChoice context,
                               List<UUID> chosenIds) {
        advance(gameData, context.sourceCard(), context.effect(), chosenIds, context.remainingPlayerIds());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void advance(GameData gameData, Card sourceCard,
                         AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect effect,
                         List<UUID> newlyChosenIds, List<UUID> remaining) {
        List<UUID> accumulated = new ArrayList<>(effect.acceptedSacrificeIds());
        accumulated.addAll(newlyChosenIds);
        if (!remaining.isEmpty()) {
            effectHandler.promptNext(gameData, sourceCard,
                    new AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect(
                            effect.count(), remaining, effect.abilityControllerId(), effect.sourcePermanentId(),
                            List.copyOf(accumulated)));
            return;
        }
        if (!accumulated.isEmpty()) {
            sacrificeEverythingChosen(gameData, sourceCard, effect, accumulated);
        }
    }

    /**
     * Sacrifices every creature chosen by the accepting players at the same time (CR 101.4 order of choice,
     * all chosen creatures leave together), then the source. Nothing changes before those creatures are
     * sacrificed, so their competing death replacements are asked about first (CR 616.1).
     */
    private void sacrificeEverythingChosen(GameData gameData, Card sourceCard,
                                           AnyPlayerMaySacrificeCreaturesThenSacrificeSourceEffect effect,
                                           List<UUID> chosenIds) {
        List<UUID> all = new ArrayList<>(effect.acceptedSacrificeIds());
        for (UUID id : chosenIds) {
            if (!all.contains(id)) {
                all.add(id);
            }
        }
        destructionSupport.performSimultaneousSacrifice(gameData, all);
        gameData.deathReplacementsCanRerunEffect = false;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                effect.abilityControllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.<CardEffect>of(new SacrificeSelfEffect())),
                effect.abilityControllerId(),
                effect.sourcePermanentId());
        effectResolutionService.resolveEffects(gameData, entry);
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
