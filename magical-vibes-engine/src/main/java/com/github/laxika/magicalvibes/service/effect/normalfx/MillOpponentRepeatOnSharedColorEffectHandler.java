package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillOpponentRepeatOnSharedColorEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Sphinx's Tutelage: the opponent mills {@code count} cards, and the whole process repeats while two
 * nonland cards that share a color were milled by the latest iteration. The loop also ends when the
 * library runs out of cards (fewer than two milled).
 */
@Component
@RequiredArgsConstructor
public class MillOpponentRepeatOnSharedColorEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillOpponentRepeatOnSharedColorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int count = ((MillOpponentRepeatOnSharedColorEffect) effect).count();
        UUID opponentId = gameQueryService.getOpponentId(gameData, entry.getControllerId());
        if (opponentId == null) {
            return;
        }

        while (true) {
            List<Card> milled = graveyardService.resolveMillPlayer(gameData, opponentId, count);
            if (milled == null || !twoNonlandCardsShareAColor(milled)) {
                return;
            }
        }
    }

    /** True when any two of the milled nonland cards have at least one color in common. */
    private boolean twoNonlandCardsShareAColor(List<Card> milled) {
        List<Card> nonlands = new ArrayList<>();
        for (Card card : milled) {
            if (!card.hasType(CardType.LAND)) {
                nonlands.add(card);
            }
        }

        for (int i = 0; i < nonlands.size(); i++) {
            List<?> colors = nonlands.get(i).getColors();
            if (colors == null || colors.isEmpty()) continue;
            for (int j = i + 1; j < nonlands.size(); j++) {
                List<?> other = nonlands.get(j).getColors();
                if (other != null && other.stream().anyMatch(colors::contains)) {
                    return true;
                }
            }
        }
        return false;
    }
}
