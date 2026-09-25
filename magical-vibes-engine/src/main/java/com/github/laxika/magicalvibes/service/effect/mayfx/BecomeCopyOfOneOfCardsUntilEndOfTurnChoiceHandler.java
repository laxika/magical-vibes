package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfCardUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfOneOfCardsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.BecomeCopyOfCardUntilEndOfTurnEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BecomeCopyOfOneOfCardsUntilEndOfTurnChoiceHandler implements MayEffectHandlerBean {

    private final BecomeCopyOfCardUntilEndOfTurnEffectHandler copyEffectHandler;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return BecomeCopyOfOneOfCardsUntilEndOfTurnEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (!accepted) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        BecomeCopyOfOneOfCardsUntilEndOfTurnEffect effect = ability.effects().stream()
                .filter(BecomeCopyOfOneOfCardsUntilEndOfTurnEffect.class::isInstance)
                .map(BecomeCopyOfOneOfCardsUntilEndOfTurnEffect.class::cast)
                .findFirst()
                .orElseThrow();
        if (effect.cards().isEmpty()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (effect.cards().size() == 1) {
            BecomeCopyOfCardUntilEndOfTurnEffect copyEffect =
                    new BecomeCopyOfCardUntilEndOfTurnEffect(effect.cards().getFirst());
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    ability.controllerId(),
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(copyEffect)),
                    null,
                    ability.sourcePermanentId());
            copyEffectHandler.resolve(gameData, entry, copyEffect);
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
            return;
        }

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < effect.cards().size(); i++) {
            validIndices.add(i);
        }
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(ability.controllerId(), validIndices,
                        GraveyardChoiceDestination.COPY_FROM_LEAVING_GRAVEYARD,
                        "Choose an artifact or creature card that left your graveyard to copy.")
                .cardPool(effect.cards())
                .mayAbilityContext(ability.sourceCard(), ability.controllerId(),
                        List.of(effect), ability.sourcePermanentId())
                .build());
    }
}
