package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyMakeMatchingHandAndLibraryCardsCreatureEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

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

    private void modifyZone(GameData gameData, StackEntry entry,
                            PerpetuallyMakeMatchingHandAndLibraryCardsCreatureEffect effect,
                            List<Card> zone) {
        for (int i = 0; i < zone.size(); i++) {
            Card card = zone.get(i);
            if (!predicateEvaluationService.matchesCardPredicate(
                    card, effect.filter(), entry.getCard().getId(), gameData, entry.getControllerId())) {
                continue;
            }

            Card modified = card.createRuntimeCopy();
            EnumSet<CardType> retainedTypes = EnumSet.noneOf(CardType.class);
            if (modified.getType() != null) {
                retainedTypes.add(modified.getType());
            }
            retainedTypes.addAll(modified.getAdditionalTypes());
            retainedTypes.remove(CardType.CREATURE);
            modified.setType(CardType.CREATURE);
            modified.setAdditionalTypes(retainedTypes);

            ArrayList<CardSubtype> subtypes = new ArrayList<>(modified.getSubtypes());
            if (!subtypes.contains(effect.subtype())) {
                subtypes.add(effect.subtype());
            }
            modified.setSubtypes(subtypes);
            modified.setPower(effect.power());
            modified.setToughness(effect.toughness());

            EnumSet<Keyword> keywords = modified.getKeywords().isEmpty()
                    ? EnumSet.noneOf(Keyword.class)
                    : EnumSet.copyOf(modified.getKeywords());
            keywords.addAll(effect.keywords());
            modified.setKeywords(keywords);
            modified.freeze();
            zone.set(i, modified);
        }
    }
}
