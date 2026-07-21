package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileOtherSpellsAndCounterAbilitiesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Summary Dismissal: exile every other spell still on the stack (including uncounterable ones), and
 * counter every activated/triggered ability. The resolving spell is already off the stack when this
 * runs ({@code resolveTopOfStack}), so "other" = everything remaining.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileOtherSpellsAndCounterAbilitiesEffectHandler implements NormalEffectHandlerBean {

    private static final Set<StackEntryType> SPELL_TYPES = Set.of(
            StackEntryType.CREATURE_SPELL, StackEntryType.ENCHANTMENT_SPELL,
            StackEntryType.SORCERY_SPELL, StackEntryType.INSTANT_SPELL,
            StackEntryType.ARTIFACT_SPELL, StackEntryType.PLANESWALKER_SPELL);

    private final CounterSupport counterSupport;
    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final StateTriggerService stateTriggerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileOtherSpellsAndCounterAbilitiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // Snapshot — removing mutates the stack.
        List<StackEntry> remaining = new ArrayList<>(gameData.stack);

        for (StackEntry se : remaining) {
            if (!gameData.stack.contains(se)) {
                continue;
            }

            StackEntryType type = se.getEntryType();
            if (type == StackEntryType.ACTIVATED_ABILITY || type == StackEntryType.TRIGGERED_ABILITY) {
                if (gameQueryService.isUncounterable(gameData, se.getCard())) {
                    log.info("Game {} - {}'s ability cannot be countered", gameData.id, se.getCard().getName());
                    continue;
                }
                if (gameQueryService.isProtectedFromCounterBySpellColor(gameData, se.getControllerId(), entry)) {
                    log.info("Game {} - {}'s ability cannot be countered by {} spells",
                            gameData.id, se.getCard().getName(),
                            entry.getCard().getColor().name().toLowerCase());
                    continue;
                }
                counterSupport.counterSpell(gameData, entry, se);
                continue;
            }

            if (!SPELL_TYPES.contains(type)) {
                continue;
            }

            // Exile — not a counter event (uncounterable spells still leave; Guile does not apply).
            gameData.stack.remove(se);
            stateTriggerService.cleanupResolvedStateTrigger(gameData, se);
            if (!se.isCopy()) {
                exileService.exileCard(gameData, se.getControllerId(), se.getCard());
            }
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(se.getCard(), " is exiled."));
            log.info("Game {} - {} exiled from stack by {}",
                    gameData.id, se.getCard().getName(), entry.getCard().getName());
        }
    }
}
