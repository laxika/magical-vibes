package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AnyColorManaChoiceSupport;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves a land-tap triggered mana ability's "that player adds one mana of any type that land produced" from the
 * stack entry: the types the tap produced ({@link TappedLandManaSupport#producedManaTypes}), or the five colors for
 * "one mana of any color" (CR 106.1a), added to the player who tapped the land. When there are several to choose from,
 * the player chooses; the ability's effects after this one wait for the choice and resolve once it is made.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddOneOfEachManaTypeProducedByLandEffectHandler implements NormalEffectHandlerBean {

    private static final List<ManaColor> FIVE_COLORS =
            List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED, ManaColor.GREEN);

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddOneOfEachManaTypeProducedByLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var mana = (AddOneOfEachManaTypeProducedByLandEffect) effect;
        UUID recipientId = TappedLandManaSupport.manaRecipient(entry);
        ManaPool pool = gameData.playerManaPools.get(recipientId);
        if (pool == null) {
            return;
        }
        List<ManaColor> choices = mana.anyColor() ? FIVE_COLORS
                : TappedLandManaSupport.producedManaTypes(entry,
                        TappedLandManaSupport.tappedLand(gameData, gameQueryService, entry)).stream().sorted().toList();
        if (choices.isEmpty()) {
            return;
        }
        String playerName = gameData.playerIdToName.get(recipientId);
        if (choices.size() == 1) {
            ManaColor color = choices.getFirst();
            pool.add(color);
            gameLogService.append(gameData, GameLog.text(playerName + " adds 1 additional "
                    + color.name().toLowerCase() + " mana."));
            log.info("Game {} - {} adds 1 additional {} mana", gameData.id, playerName, color);
            return;
        }
        StackEntry rest = takeRemainingEffects(entry);
        ChoiceContext context = rest == null
                ? ChoiceContext.ManaColorChoice.fixedColorCombination(recipientId, false, 1, choices)
                : new ChoiceContext.FixedManaColorThenEffectsChoice(recipientId, choices, 1, rest);
        AnyColorManaChoiceSupport.beginOrQueueChoice(interactionHandlerRegistry, gameData,
                new PendingInteraction.ColorChoice(recipientId, null, null, context,
                        choices.stream().map(Enum::name).toList(), mana.anyColor()
                                ? "Choose a color of mana to add." : "Choose a type of mana the land produced."));
        log.info("Game {} - Awaiting {} to choose the additional mana", gameData.id, playerName);
    }

    /**
     * Takes the effects after the resolving one off {@code entry} into a follow-up that resolves once the mana's type
     * is chosen ("… and this deals 1 damage to them", "then that land deals 1 damage"); {@code null} when none follow.
     */
    private static StackEntry takeRemainingEffects(StackEntry entry) {
        int index = entry.getResolvingEffectIndex();
        List<CardEffect> effects = entry.getEffectsToResolve();
        if (index < 0 || index >= effects.size() - 1) {
            return null;
        }
        StackEntry followUp = new StackEntry(entry);
        followUp.replaceEffectsToResolve(effects.subList(index + 1, effects.size()));
        entry.replaceEffectsToResolve(effects.subList(0, index + 1));
        return followUp;
    }
}
