package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VillainousChoiceState;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentFacesSycoraxCommanderVillainousChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsHandThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Sycorax Commander's opponent-by-opponent villainous choice in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachOpponentFacesSycoraxCommanderVillainousChoiceEffectHandler
        implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TargetPlayerDiscardsHandThenDrawsThatManyEffectHandler discardHandler;
    private final DealDamageToPlayersEffectHandler damageHandler;
    private final VillainousChoiceSupport villainousChoiceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentFacesSycoraxCommanderVillainousChoiceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (!state.active) {
            state.reset();
            state.active = true;
            state.remaining.addAll(EachPlayerMayScryEffectHandler
                    .apnapOpponents(gameData, entry.getControllerId()));
        }

        if (state.chosenMode != null) {
            String chosen = state.chosenMode;
            state.chosenMode = null;
            if (EachOpponentFacesSycoraxCommanderVillainousChoiceEffect.DISCARD_OPTION.equals(chosen)) {
                discard(gameData, entry, state.currentPlayerId);
            } else {
                dealDamage(gameData, entry, state.currentPlayerId);
            }
            advance(gameData, entry);
            return;
        }

        advance(gameData, entry);
    }

    private void advance(GameData gameData, StackEntry entry) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (villainousChoiceSupport.repeatIfNeeded(gameData)) {
            return;
        }
        UUID opponentId = state.remaining.pollFirst();
        if (opponentId == null) {
            state.reset();
            gameData.rerunCurrentEffectAfterInteraction = false;
            return;
        }

        state.currentPlayerId = opponentId;
        gameData.rerunCurrentEffectAfterInteraction = true;
        villainousChoiceSupport.beginChoice(gameData, opponentId, entry.getCard().getName(),
                EachOpponentFacesSycoraxCommanderVillainousChoiceEffect.DISCARD_OPTION,
                List.of(EachOpponentFacesSycoraxCommanderVillainousChoiceEffect.DISCARD_OPTION,
                        EachOpponentFacesSycoraxCommanderVillainousChoiceEffect.DAMAGE_OPTION),
                entry.getCard().getName() + " — Choose a villainous choice.");
    }

    private void discard(GameData gameData, StackEntry entry, UUID opponentId) {
        StackEntry discardEntry = new StackEntry(
                entry.getEntryType(), entry.getCard(), entry.getControllerId(), entry.getDescription(),
                List.of(), opponentId, entry.getSourcePermanentId());
        discardEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        discardHandler.resolve(gameData, discardEntry,
                new TargetPlayerDiscardsHandThenDrawsThatManyEffect(1));
    }

    private void dealDamage(GameData gameData, StackEntry entry, UUID opponentId) {
        StackEntry damageEntry = new StackEntry(
                entry.getEntryType(), entry.getCard(), entry.getControllerId(), entry.getDescription(),
                List.of(), opponentId, entry.getSourcePermanentId());
        damageEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        damageHandler.resolve(gameData, damageEntry,
                new DealDamageToPlayersEffect(new CardsInHand(CountScope.TARGET_PLAYER),
                        DamageRecipient.TARGET_PLAYER));
    }
}
