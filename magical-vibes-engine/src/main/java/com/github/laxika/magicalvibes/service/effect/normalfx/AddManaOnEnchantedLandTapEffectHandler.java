package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddManaOnEnchantedLandTapEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardChosenColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AnyColorManaChoiceSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves "whenever enchanted land is tapped for mana, its controller adds …" from the stack entry of the triggered
 * mana ability (CR 605.4a): the land's controller (as it is on the battlefield, else the player who tapped it) adds the
 * mana of the wrapped effect, with its amount evaluated for that player and the Aura (live, else as it was when the
 * ability triggered): a fixed color, any color or a choice among colors (that player chooses), or the Aura's chosen
 * color.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddManaOnEnchantedLandTapEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddManaOnEnchantedLandTapEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var mana = ((AddManaOnEnchantedLandTapEffect) effect).mana();
        UUID recipientId = TappedLandManaSupport.tappedPermanentController(gameData, gameQueryService, entry);
        Permanent source = TappedLandManaSupport.sourcePermanent(gameData, gameQueryService, entry);
        if (recipientId == null || gameData.playerManaPools.get(recipientId) == null) {
            return;
        }
        String playerName = gameData.playerIdToName.get(recipientId);
        AmountContext amountContext = new AmountContext(recipientId, source, null, 0, 0);

        if (mana instanceof AwardAnyColorManaEffect anyColor) {
            int amount = amountEvaluationService.evaluate(gameData, anyColor.amount(), amountContext);
            boolean snowSource = source != null
                    && gameQueryService.hasEffectiveSupertype(gameData, source, CardSupertype.SNOW);
            if (AnyColorManaChoiceSupport.beginColorChoice(interactionHandlerRegistry, gameData,
                    recipientId, anyColor, amount, false, null, null, null, null, snowSource)) {
                gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                        " triggers — " + playerName + " chooses a color of mana to add."));
                log.info("Game {} - Awaiting {} to choose a mana color from {}", gameData.id, playerName,
                        entry.getCard().getName());
            }
            return;
        }
        if (mana instanceof AwardManaEffect award) {
            int amount = amountEvaluationService.evaluate(gameData, award.amount(), amountContext);
            TappedLandManaSupport.addAdditionalMana(gameData, gameLogService, entry, recipientId, award.color(),
                    amount);
            return;
        }
        if (mana instanceof AwardManaOfColorsEffect ofColors) {
            int amount = amountEvaluationService.evaluate(gameData, ofColors.amount(), amountContext);
            if (amount <= 0 || ofColors.colors().isEmpty()) {
                return;
            }
            if (ofColors.colors().size() == 1) {
                TappedLandManaSupport.addAdditionalMana(gameData, gameLogService, entry, recipientId,
                        ofColors.colors().getFirst(), amount);
                return;
            }
            ChoiceContext.ManaColorChoice choiceContext = ChoiceContext.ManaColorChoice
                    .fixedColorCombination(recipientId, false, amount, ofColors.colors());
            List<String> colors = ofColors.colors().stream().map(Enum::name).toList();
            AnyColorManaChoiceSupport.beginOrQueueChoice(interactionHandlerRegistry, gameData,
                    new PendingInteraction.ColorChoice(recipientId, null, null, choiceContext, colors,
                            "Choose a color of mana to add."));
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " triggers — " + playerName + " chooses colors of mana to add."));
            return;
        }
        if (mana instanceof AwardChosenColorManaEffect) {
            CardColor chosenColor = source == null ? null : source.getChosenColor();
            if (chosenColor != null) {
                TappedLandManaSupport.addAdditionalMana(gameData, gameLogService, entry, recipientId,
                        ManaColor.valueOf(chosenColor.name()), 1);
            }
            return;
        }
        log.warn("Unsupported mana effect in AddManaOnEnchantedLandTapEffect: {}", mana.getClass().getSimpleName());
    }
}
