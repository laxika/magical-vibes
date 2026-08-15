package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayScryEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerMayScryEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ScryEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies an accepted {@link EachPlayerMayScryEffect} choice and advances to the next player. */
@Component
@RequiredArgsConstructor
public class EachPlayerMayScryHandler implements MayEffectHandlerBean {

    private final EachPlayerMayScryEffectHandler effectHandler;
    private final ScryEffectHandler scryEffectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayScryEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        EachPlayerMayScryEffect effect = (EachPlayerMayScryEffect) ability.effects().getFirst();
        List<UUID> remaining = effect.remainingPlayerIds().subList(1, effect.remainingPlayerIds().size());

        if (accepted) {
            ScryEffect scry = new ScryEffect(effect.count());
            StackEntry scryEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    player.getId(),
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(scry)),
                    0,
                    ability.sourcePermanentId());
            scryEntry.setSourcePermanentSnapshot(ability.sourcePermanentSnapshot());
            scryEffectHandler.resolve(gameData, scryEntry, scry);
        }

        if (!remaining.isEmpty()) {
            effectHandler.promptNext(gameData, ability.sourceCard(),
                    new EachPlayerMayScryEffect(effect.count(), remaining));
        }

        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
        }
    }
}
