package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TaintedPactCardChoiceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.TaintedPactSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Handles the per-card hand choice during Tainted Pact resolution. */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaintedPactCardChoiceHandler implements MayEffectHandlerBean {

    private final TaintedPactSupport support;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TaintedPactCardChoiceEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        TaintedPactCardChoiceEffect effect = ability.effects().stream()
                .filter(e -> e instanceof TaintedPactCardChoiceEffect)
                .map(e -> (TaintedPactCardChoiceEffect) e)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing Tainted Pact card choice effect"));

        ExiledCardEntry exiledEntry = ability.targetCardId() == null
                ? null
                : gameData.findExiledCard(ability.targetCardId());
        Card exiledCard = exiledEntry == null ? null : exiledEntry.card();

        if (accepted && exiledCard != null) {
            gameData.removeFromExile(exiledCard.getId());
            gameData.addCardToHand(player.getId(), exiledCard);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " puts ", exiledCard, " into their hand (Tainted Pact)."));
            log.info("Game {} - {} puts {} into hand via Tainted Pact",
                    gameData.id, player.getUsername(), exiledCard.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        if (exiledCard != null) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " leaves ", exiledCard, " in exile (Tainted Pact)."));
        }

        support.exileTopCardAndOfferToHand(
                gameData, ability.sourceCard(), player.getId(), effect.exiledNames());
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
