package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangeColorTextEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChangeColorTextEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID targetId = entry.getTargetId();
        boolean colorWordsAllowed = !(effect instanceof ChangeColorTextEffect e) || e.colorWordsAllowed();
        boolean landTypesAllowed = effect instanceof ChangeColorTextEffect e && e.landTypesAllowed();

        // Target may be a permanent (Mind Bend) or, for Glamerdye/Magical Hack, a spell still on the stack.
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null && gameQueryService.findStackEntryByCardId(gameData, targetId) == null) {
            return;
        }

        ChoiceContext.TextChangeFromWord choiceContext = new ChoiceContext.TextChangeFromWord(targetId);

        List<String> options = new ArrayList<>();
        if (colorWordsAllowed) {
            options.addAll(GameQueryService.TEXT_CHANGE_COLOR_WORDS);
        }
        if (landTypesAllowed) {
            options.addAll(GameQueryService.TEXT_CHANGE_LAND_TYPES);
        }
        String prompt;
        if (colorWordsAllowed && landTypesAllowed) {
            prompt = "Choose a color word or basic land type to replace.";
        } else if (landTypesAllowed) {
            prompt = "Choose a basic land type to replace.";
        } else {
            prompt = "Choose a color word to replace.";
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                entry.getControllerId(), null, null, choiceContext, options, prompt));

        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        log.info("Game {} - Awaiting {} to choose a color word or basic land type for text change", gameData.id, playerName);
    
    }
}
