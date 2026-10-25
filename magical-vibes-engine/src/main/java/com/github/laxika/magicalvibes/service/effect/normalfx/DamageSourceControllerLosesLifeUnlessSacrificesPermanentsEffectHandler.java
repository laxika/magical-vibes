package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EachPlayerSacrificeOrLoseLifeState;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the Archfiend of Spite damage trigger: the damaging source's controller loses life unless they sacrifice
 * that many permanents.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: a run changes nothing before its loss or its sacrifice (asking the
 * player or starting the sacrifice choice changes nothing yet), so a Lich's Duel Mastery that loss would sacrifice is
 * asked about (CR 616.1) before any life is lost, and the run is resolved again with that choice.
 */
@Component
@RequiredArgsConstructor
public class DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffectHandler
        implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private static final String SACRIFICE_SELECTION_PENDING = "__sacrifice_selection_pending";

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeSupport lifeSupport;
    private final PlayerInputService playerInputService;
    private final StepDepartureSupport stepDepartureSupport;

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
            List<UUID> legalIds = legalPermanentIds(gameData, entry, playerId);
            if (legalIds.size() < amount) {
                loseLife(gameData, state, playerId, amount, entry);
                return;
            }

            state.reset();
            state.active = true;
            state.currentPlayerId = playerId;
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

        // The chosen mode stays set until the loss is done, so a rerun after the Mastery's question loses it again.
        if (!sacrificeOption(amount).equals(state.chosenMode)) {
            loseLife(gameData, state, playerId, amount, entry);
            return;
        }

        List<UUID> legalIds = legalPermanentIds(gameData, entry, playerId);
        if (legalIds.size() < amount) {
            loseLife(gameData, state, playerId, amount, entry);
        } else if (legalIds.size() == amount) {
            // They are sacrificed together as this step's last action, so a CR 616.1 question ends the step before
            // any of them leaves.
            finish(state, gameData);
            stepDepartureSupport.depart(gameData, entry, effect,
                    DepartureBatch.of(StepDepartureSupport.sacrifices(legalIds)));
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
                permanent -> !gameQueryService.cantBeChosenToSacrifice(gameData, permanent));
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
