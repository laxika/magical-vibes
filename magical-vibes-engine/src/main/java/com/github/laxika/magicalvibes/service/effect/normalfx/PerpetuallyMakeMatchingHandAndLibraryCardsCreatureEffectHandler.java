package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyMakeMatchingHandAndLibraryCardsCreatureEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PerpetuallyMakeMatchingHandAndLibraryCardsCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyMakeMatchingHandAndLibraryCardsCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var change = (PerpetuallyMakeMatchingHandAndLibraryCardsCreatureEffect) effect;
        modifyZone(gameData, entry, change,
                gameData.playerHands.getOrDefault(entry.getControllerId(), List.of()));
        modifyZone(gameData, entry, change,
                gameData.playerDecks.getOrDefault(entry.getControllerId(), List.of()));
    }

    /**
     * Each matching card perpetually becomes a creature of the subtype in addition to its other types, with the base
     * power and toughness and the keywords: changes to the physical card, kept in the perpetual stores, not to its
     * copiable values (CR 707.2).
     */
    private void modifyZone(GameData gameData, StackEntry entry,
                            PerpetuallyMakeMatchingHandAndLibraryCardsCreatureEffect effect,
                            List<Card> zone) {
        for (Card card : List.copyOf(zone)) {
            if (!predicateEvaluationService.matchesCardPredicate(
                    card, effect.filter(), entry.getCard().getId(), gameData, entry.getControllerId())) {
                continue;
            }
            PerpetuallyGrantCardCharacteristicsEffectHandler.grant(gameData, card.getId(),
                    new PerpetuallyGrantCardCharacteristicsEffect(
                            Set.of(CardType.CREATURE), Set.of(effect.subtype()), List.of()));
            gameData.perpetualCardBasePowerToughness.put(card.getId(), new GameData.PerpetualBasePowerToughness(
                    effect.power(), effect.toughness(), gameData.nextTimestamp()));
            gameData.grantPerpetualKeywords(card.getId(), effect.keywords());
        }
    }
}
