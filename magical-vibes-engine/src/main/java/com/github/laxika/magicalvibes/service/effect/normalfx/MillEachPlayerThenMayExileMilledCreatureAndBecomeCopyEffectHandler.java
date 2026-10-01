package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves Shadow Kin's upkeep mill and optional creature-copy choice. */
@Component
@RequiredArgsConstructor
public class MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffectHandler
        implements NormalEffectHandlerBean {

    private static final int MILL_COUNT = 3;

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> milledCreatureCards = new ArrayList<>();
        for (var playerId : gameData.orderedPlayerIds) {
            List<Card> milled = graveyardService.resolveMillPlayer(gameData, playerId, MILL_COUNT);
            milledCreatureCards.addAll(milled.stream()
                    .filter(card -> card.hasType(CardType.CREATURE))
                    .filter(card -> gameQueryService.findCardInGraveyardById(gameData, card.getId()) != null)
                    .toList());
        }

        if (!milledCreatureCards.isEmpty()) {
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    entry.getCard(),
                    entry.getControllerId(),
                    List.of(new MillEachPlayerThenMayExileMilledCreatureAndBecomeCopyEffect(milledCreatureCards)),
                    "You may exile a creature card milled this way and have "
                            + entry.getCard().getName() + " become a copy of it.",
                    null,
                    null,
                    entry.getSourcePermanentId()));
        }
    }
}
