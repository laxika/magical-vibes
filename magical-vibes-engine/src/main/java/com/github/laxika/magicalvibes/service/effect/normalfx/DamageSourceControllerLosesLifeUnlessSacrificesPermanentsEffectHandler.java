package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EachPlayerSacrificeOrLoseLifeState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the Archfiend of Spite damage trigger. */
@Component
@RequiredArgsConstructor
public class DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffectHandler
        implements NormalEffectHandlerBean {

    private static final String SACRIFICE_SELECTION_PENDING = "__sacrifice_selection_pending";

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeSupport lifeSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var trigger = (DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect) effect;
        UUID playerId = trigger.sacrificingPlayerId();
        int amount = trigger.amount();
        EachPlayerSacrificeOrLoseLifeState state = gameData.eachPlayerSacrificeOrLoseLife;

        if (playerId == null || amount <= 0 || !gameData.playerIds.contains(playerId)) {
            finish(state, gameData);
            return;
        }

        if (!state.active) {
            state.reset();
            state.active = true;
            state.currentPlayerId = playerId;
            List<UUID> legalIds = legalPermanentIds(gameData, entry, playerId);
            if (legalIds.size() < amount) {
                loseLife(gameData, state, playerId, amount, entry);
                return;
            }

            gameData.rerunCurrentEffectAfterInteraction = true;
            String sacrificeOption = sacrificeOption(amount);
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    playerId, null, null,
                    new ChoiceContext.EachPlayerSacrificeOrLoseLifeChoice(playerId,
                            entry.getCard().getName()),
                    List.of(sacrificeOption, ChoiceContext.EachPlayerSacrificeOrLoseLifeChoice
                            .loseLife(amount)),
                    entry.getCard().getName() + " — " + sacrificeOption.toLowerCase()
                            + " or lose " + amount + " life."));
            return;
        }

        if (SACRIFICE_SELECTION_PENDING.equals(state.chosenMode)) {
            finish(state, gameData);
            return;
        }

        if (state.chosenMode == null) {
            finish(state, gameData);
            return;
        }

        String chosenMode = state.chosenMode;
        state.chosenMode = null;
        if (!sacrificeOption(amount).equals(chosenMode)) {
            loseLife(gameData, state, playerId, amount, entry);
            return;
        }

        List<UUID> legalIds = legalPermanentIds(gameData, entry, playerId);
        if (legalIds.size() < amount) {
            loseLife(gameData, state, playerId, amount, entry);
        } else if (legalIds.size() == amount) {
            for (UUID permanentId : legalIds) {
                Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
                if (permanent != null) {
                    destructionSupport.sacrificeAndLog(gameData, permanent, playerId);
                }
            }
            finish(state, gameData);
        } else {
            state.chosenMode = SACRIFICE_SELECTION_PENDING;
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginMultiPermanentChoice(gameData, playerId, legalIds, amount,
                    new MultiPermanentChoiceContext.ForcedSacrifice(playerId, List.of(), List.of()),
                    "Choose " + amount + " permanent" + (amount > 1 ? "s" : "") + " to sacrifice.");
        }
    }

    private List<UUID> legalPermanentIds(GameData gameData, StackEntry entry, UUID playerId) {
        if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, entry.getControllerId())) {
            return List.of();
        }
        return destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent));
    }

    private void loseLife(GameData gameData, EachPlayerSacrificeOrLoseLifeState state,
            UUID playerId, int amount, StackEntry entry) {
        lifeSupport.applyLifeLoss(gameData, playerId, amount, entry.getCard().getName());
        finish(state, gameData);
    }

    private void finish(EachPlayerSacrificeOrLoseLifeState state, GameData gameData) {
        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }

    private String sacrificeOption(int amount) {
        return ChoiceContext.EachPlayerSacrificeOrLoseLifeChoice.sacrifice(
                amount + " permanent" + (amount > 1 ? "s" : ""));
    }
}
