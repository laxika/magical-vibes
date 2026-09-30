package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardAndBecomeCopyEffect;
import com.github.laxika.magicalvibes.model.effect.MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

/** Starts Shadow Kin's optional selection of a creature card milled by its ability. */
@Component
@RequiredArgsConstructor
public class MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyChoiceHandler
        implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (!accepted) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect effect = ability.effects().stream()
                .filter(MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect.class::isInstance)
                .map(MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect.class::cast)
                .findFirst()
                .orElseThrow();
        List<Card> candidates = effect.milledCreatureCards().stream()
                .filter(card -> gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null)
                .toList();
        if (candidates.isEmpty()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        List<Integer> validIndices = IntStream.range(0, candidates.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(ability.controllerId(), validIndices, GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        "Choose a creature card milled this way to exile.")
                .cardPool(candidates)
                .mayAbilityContext(
                        ability.sourceCard(),
                        ability.controllerId(),
                        List.of(new ExileTargetCreatureCardFromGraveyardAndBecomeCopyEffect(
                                EffectSlot.UPKEEP_TRIGGERED)),
                        ability.sourcePermanentId())
                .build());
    }
}
