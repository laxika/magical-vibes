package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves "add … mana. Until end of turn, you don't lose this mana as steps and phases end": the amount is read as the
 * effect resolves (an attacker count locked as the ability triggered is the entry's X); one color is added at once,
 * several are chosen by the recipient, one color for all of it or a color for each mana, and the chosen mana is added
 * as the prompt is answered. Nothing is added for an amount of zero or less.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwardPersistentManaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardPersistentManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AwardPersistentManaEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));
        if (amount <= 0) {
            return;
        }

        UUID recipientId = switch (e.recipient()) {
            case TARGET_PLAYER -> entry.getTargetId() != null
                    ? entry.getTargetId() : entry.getControllerId();
            case CONTROLLER -> entry.getControllerId();
        };
        if (e.colors().size() > 1) {
            beginColorChoice(gameData, e, recipientId, amount);
            return;
        }
        ManaPool pool = gameData.playerManaPools.get(recipientId);
        ManaColor color = ManaProductionSupport.effectiveColor(gameData, entry.getControllerId(), e.colors().getFirst());
        pool.addPersistentMana(color, amount);

        String playerName = gameData.playerIdToName.get(recipientId);
        gameLogService.append(gameData, GameLog.text(
                playerName + " adds " + amount + " " + color.getCode() + "."));
        log.info("Game {} - {} adds {} {} that persists until end of turn",
                gameData.id, playerName, amount, color);
    }

    private void beginColorChoice(GameData gameData, AwardPersistentManaEffect e, UUID recipientId, int amount) {
        List<String> options = e.colors().stream().map(Enum::name).toList();
        if (e.restriction() == ManaSpendRestriction.SPELL_ONLY) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(recipientId, null, null,
                    new ChoiceContext.PersistentSpellOnlyManaColorChoice(recipientId, amount,
                            e.eachChosenSeparately()),
                    options, "Choose a color of mana to add (spells only)."));
        } else if (e.eachChosenSeparately()) {
            interactionHandlerRegistry.begin(gameData,
                    new ChoiceContext.PersistentManaDistributionChoice(recipientId, amount, e.colors()).prompt());
        } else {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(recipientId, null, null,
                    new ChoiceContext.PersistentManaColorChoice(recipientId, amount),
                    options, "Choose a color of mana to add."));
        }
        log.info("Game {} - Awaiting {} to choose the colors of {} persistent mana among {}",
                gameData.id, gameData.playerIdToName.get(recipientId), amount, e.colors());
    }
}
