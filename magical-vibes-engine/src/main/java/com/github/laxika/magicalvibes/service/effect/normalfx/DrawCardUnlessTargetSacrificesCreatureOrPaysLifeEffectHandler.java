package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.IndulgentTormentorState;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardUnlessTargetSacrificesCreatureOrPaysLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the target opponent's three-way choice for Indulgent Tormentor. The sacrifice choice
 * uses the existing permanent-choice path and re-enters this handler after the chosen creature is
 * sacrificed.
 */
@Component
@RequiredArgsConstructor
public class DrawCardUnlessTargetSacrificesCreatureOrPaysLifeEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final LifeSupport lifeSupport;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardUnlessTargetSacrificesCreatureOrPaysLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawCardUnlessTargetSacrificesCreatureOrPaysLifeEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            gameData.indulgentTormentor.reset();
            return;
        }

        IndulgentTormentorState state = gameData.indulgentTormentor;
        if (!state.active) {
            state.reset();
            state.active = true;
            offerChoice(gameData, entry, e, targetPlayerId);
            return;
        }

        if (state.waitingForSacrifice) {
            finish(gameData, state);
            return;
        }

        if (state.chosenMode != null) {
            String chosenMode = state.chosenMode;
            state.chosenMode = null;
            applyChoice(gameData, entry, e, targetPlayerId, chosenMode);
            return;
        }

        finish(gameData, state);
    }

    private void offerChoice(GameData gameData, StackEntry entry,
            DrawCardUnlessTargetSacrificesCreatureOrPaysLifeEffect effect, UUID targetPlayerId) {
        List<String> options = new ArrayList<>();
        if (!creatureIds(gameData, targetPlayerId).isEmpty()) {
            options.add(ChoiceContext.IndulgentTormentorChoice.SACRIFICE);
        }
        if (canPayLife(gameData, targetPlayerId, effect.lifeCost())) {
            options.add(ChoiceContext.IndulgentTormentorChoice.payLife(effect.lifeCost()));
        }
        options.add(ChoiceContext.IndulgentTormentorChoice.DRAW);

        if (options.size() == 1) {
            draw(gameData, entry);
            gameData.indulgentTormentor.reset();
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = true;
        String sourceName = entry.getCard().getName();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                targetPlayerId, null, null,
                new ChoiceContext.IndulgentTormentorChoice(targetPlayerId, effect.lifeCost(), sourceName),
                options,
                sourceName + " - choose sacrifice a creature, pay " + effect.lifeCost()
                        + " life, or draw a card."));
    }

    private void applyChoice(GameData gameData, StackEntry entry,
            DrawCardUnlessTargetSacrificesCreatureOrPaysLifeEffect effect,
            UUID targetPlayerId, String chosenMode) {
        if (ChoiceContext.IndulgentTormentorChoice.DRAW.equals(chosenMode)) {
            draw(gameData, entry);
            finish(gameData, gameData.indulgentTormentor);
            return;
        }

        if (ChoiceContext.IndulgentTormentorChoice.payLife(effect.lifeCost()).equals(chosenMode)) {
            if (canPayLife(gameData, targetPlayerId, effect.lifeCost())) {
                lifeSupport.applyLifePayment(gameData, targetPlayerId, effect.lifeCost(), entry.getCard().getName());
                finish(gameData, gameData.indulgentTormentor);
            } else {
                draw(gameData, entry);
                finish(gameData, gameData.indulgentTormentor);
            }
            return;
        }

        if (ChoiceContext.IndulgentTormentorChoice.SACRIFICE.equals(chosenMode)) {
            List<UUID> creatureIds = creatureIds(gameData, targetPlayerId);
            if (creatureIds.isEmpty()) {
                draw(gameData, entry);
                finish(gameData, gameData.indulgentTormentor);
                return;
            }
            if (creatureIds.size() == 1) {
                Permanent creature = gameData.playerBattlefields.get(targetPlayerId).stream()
                        .filter(permanent -> permanent.getId().equals(creatureIds.getFirst()))
                        .findFirst().orElse(null);
                if (creature != null) {
                    destructionSupport.sacrificeAndLog(gameData, creature, targetPlayerId);
                }
                finish(gameData, gameData.indulgentTormentor);
                return;
            }

            gameData.indulgentTormentor.waitingForSacrifice = true;
            gameData.rerunCurrentEffectAfterInteraction = true;
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.TormentSacrifice(targetPlayerId));
            playerInputService.beginPermanentChoice(gameData, targetPlayerId, creatureIds,
                    entry.getCard().getName() + " - choose a creature to sacrifice.");
            return;
        }

        draw(gameData, entry);
        finish(gameData, gameData.indulgentTormentor);
    }

    private List<UUID> creatureIds(GameData gameData, UUID playerId) {
        return destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> gameQueryService.isCreature(gameData, permanent));
    }

    private boolean canPayLife(GameData gameData, UUID playerId, int amount) {
        return gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameData.getLife(playerId) >= amount;
    }

    private void draw(GameData gameData, StackEntry entry) {
        playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), 1);
    }

    private void finish(GameData gameData, IndulgentTormentorState state) {
        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
    }
}
