package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddManaOfTypeProducedByTappedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AnyColorManaChoiceSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves "that player adds one mana of any type that permanent produced" (Eloren Wilds, Kinnan, Roxanne) and "add an
 * additional {C}" for a tap for {C} (Forsaken Monument) from the stack entry of the triggered mana ability
 * (CR 605.4a): the types the tap produced are the entry's produced mana colors; the mana goes to the player who tapped
 * the permanent (the entry's triggering player, else its controller), who chooses among several types.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddManaOfTypeProducedByTappedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddManaOfTypeProducedByTappedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AddManaOfTypeProducedByTappedPermanentEffect trigger =
                (AddManaOfTypeProducedByTappedPermanentEffect) effect;
        UUID recipientId = TappedLandManaSupport.manaRecipient(entry);
        ManaPool pool = gameData.playerManaPools.get(recipientId);
        if (pool == null || entry.getProducedManaColors().isEmpty()) {
            return;
        }
        if (trigger.requiredColor() != null) {
            if (entry.getProducedManaColors().contains(trigger.requiredColor())) {
                add(gameData, pool, recipientId, trigger.requiredColor());
            }
            return;
        }
        if (entry.getProducedManaColors().size() == 1) {
            add(gameData, pool, recipientId, entry.getProducedManaColors().iterator().next());
            return;
        }
        List<ManaColor> choices = entry.getProducedManaColors().stream().sorted().toList();
        AnyColorManaChoiceSupport.beginOrQueueChoice(interactionHandlerRegistry, gameData,
                new PendingInteraction.ColorChoice(recipientId, null, null,
                        ChoiceContext.ManaColorChoice.fixedColorCombination(recipientId, false, 1, choices),
                        choices.stream().map(Enum::name).toList(),
                        "Choose a type of mana the permanent produced."));
        log.info("Game {} - {} awaits a choice of mana type the permanent produced", gameData.id,
                gameData.playerIdToName.get(recipientId));
    }

    private void add(GameData gameData, ManaPool pool, UUID recipientId, ManaColor manaColor) {
        pool.add(manaColor);
        String playerName = gameData.playerIdToName.get(recipientId);
        gameLogService.append(gameData, GameLog.text(playerName + " adds 1 " + manaColor.getCode() + "."));
        log.info("Game {} - {} adds 1 {}", gameData.id, playerName, manaColor);
    }
}
