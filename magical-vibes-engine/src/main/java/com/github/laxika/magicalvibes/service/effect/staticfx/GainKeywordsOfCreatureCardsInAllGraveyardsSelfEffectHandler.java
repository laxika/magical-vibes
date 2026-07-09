package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainKeywordsOfCreatureCardsInAllGraveyardsEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GainKeywordsOfCreatureCardsInAllGraveyardsSelfEffectHandler implements StaticEffectHandlerBean {

    /**
     * The keywords Cairn Wanderer scans graveyards for. Landwalk is represented by its variants;
     * protection is omitted (inherent protection is not modelled as a card characteristic).
     */
    private static final Set<Keyword> WATCHED_KEYWORDS = Set.of(
            Keyword.FLYING,
            Keyword.FEAR,
            Keyword.FIRST_STRIKE,
            Keyword.DOUBLE_STRIKE,
            Keyword.DEATHTOUCH,
            Keyword.HASTE,
            Keyword.LIFELINK,
            Keyword.REACH,
            Keyword.SHROUD,
            Keyword.TRAMPLE,
            Keyword.VIGILANCE,
            Keyword.FORESTWALK,
            Keyword.MOUNTAINWALK,
            Keyword.ISLANDWALK,
            Keyword.SWAMPWALK
    );

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainKeywordsOfCreatureCardsInAllGraveyardsEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        GameData gameData = context.gameData();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            for (Card card : graveyard) {
                if (!card.hasType(CardType.CREATURE)) continue;
                for (Keyword keyword : card.getKeywords()) {
                    if (WATCHED_KEYWORDS.contains(keyword)) {
                        accumulator.addKeyword(keyword);
                    }
                }
            }
        }
    }
}
