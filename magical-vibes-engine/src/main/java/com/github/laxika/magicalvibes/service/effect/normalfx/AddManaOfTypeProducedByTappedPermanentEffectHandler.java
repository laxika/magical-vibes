package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddManaOfTypeProducedByTappedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AnyColorManaChoiceSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves "that player adds one mana of any type that permanent produced" (Eloren Wilds, Kinnan, Roxanne), "add an
 * additional {C}" for a tap for {C} (Ultima, Forsaken Monument) and "add an additional one mana of that color" for a tap
 * for the source's chosen color (Caged Sun, Gauntlet of Power) from the stack entry of the triggered mana ability
 * (CR 605.4a): the types the tap produced are the entry's produced mana colors, and a tap that produced none of the
 * required type adds nothing (CR 106.12a). The mana goes to the player who tapped the permanent (the entry's
 * triggering player, else its controller), or for "its controller adds" to the tapped permanent's controller; that
 * player chooses among several types.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddManaOfTypeProducedByTappedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
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
        UUID recipientId = trigger.toTappedPermanentController()
                ? TappedLandManaSupport.tappedPermanentController(gameData, gameQueryService, entry)
                : TappedLandManaSupport.manaRecipient(entry);
        ManaPool pool = gameData.playerManaPools.get(recipientId);
        if (pool == null || entry.getProducedManaColors().isEmpty()) {
            return;
        }
        ManaColor requiredColor = trigger.sourceChosenColor() ? sourceChosenColor(gameData, entry)
                : trigger.requiredColor();
        if (trigger.sourceChosenColor() && requiredColor == null) {
            return;
        }
        if (requiredColor != null) {
            if (entry.getProducedManaColors().contains(requiredColor)) {
                add(gameData, pool, recipientId, requiredColor);
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

    /** The source's chosen color as mana, as the source is or was when the ability triggered; {@code null} if none. */
    private ManaColor sourceChosenColor(GameData gameData, StackEntry entry) {
        Permanent source = TappedLandManaSupport.sourcePermanent(gameData, gameQueryService, entry);
        CardColor chosen = source == null ? null : source.getChosenColor();
        return chosen == null ? null : ManaColor.valueOf(chosen.name());
    }

    private void add(GameData gameData, ManaPool pool, UUID recipientId, ManaColor manaColor) {
        pool.add(manaColor);
        String playerName = gameData.playerIdToName.get(recipientId);
        gameLogService.append(gameData, GameLog.text(playerName + " adds 1 " + manaColor.getCode() + "."));
        log.info("Game {} - {} adds 1 {}", gameData.id, playerName, manaColor);
    }
}
