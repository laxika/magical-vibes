package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerKeepsCardsShufflesRestIntoLibraryEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves "each player chooses up to N cards in their hand to keep, then shuffles the rest into
 * their library" (Worldpurge). Players choose in APNAP order (active player first); the choices
 * are driven one at a time via {@link com.github.laxika.magicalvibes.model.PendingInteraction.KeepCardsInHandChoice}
 * and applied by {@link KeepCardsInHandSupport}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerKeepsCardsShufflesRestIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final KeepCardsInHandSupport keepCardsInHandSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerKeepsCardsShufflesRestIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int keepCount = ((EachPlayerKeepsCardsShufflesRestIntoLibraryEffect) effect).keepCount();
        keepCardsInHandSupport.beginNextChoice(gameData, apnapOrder(gameData), keepCount, entry.getCard().getName());
    }

    /** Seating order rotated so the active player is first (APNAP). */
    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }
}
