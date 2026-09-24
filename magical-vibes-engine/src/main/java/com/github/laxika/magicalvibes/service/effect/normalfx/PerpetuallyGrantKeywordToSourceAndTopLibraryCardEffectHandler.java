package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordToSourceAndTopLibraryCardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Resolves perpetual keyword changes on the source card and the first matching library card. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantKeywordToSourceAndTopLibraryCardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantKeywordToSourceAndTopLibraryCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PerpetuallyGrantKeywordToSourceAndTopLibraryCardEffect grant =
                (PerpetuallyGrantKeywordToSourceAndTopLibraryCardEffect) effect;

        grantToSource(gameData, entry, grant.keyword());

        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null) {
            return;
        }

        for (int i = 0; i < library.size(); i++) {
            Card card = library.get(i);
            if (!predicateEvaluationService.matchesCardPredicate(
                    card, grant.libraryCardFilter(), entry.getCard().getId(), gameData, entry.getControllerId())) {
                continue;
            }
            library.set(i, withKeyword(card, grant.keyword()));
            return;
        }
    }

    private void grantToSource(GameData gameData, StackEntry entry, Keyword keyword) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null) {
            source.exchangeCard(withKeyword(source.getCard(), keyword));
        }
    }

    private Card withKeyword(Card card, Keyword keyword) {
        Card copy = card.createRuntimeCopy();
        EnumSet<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        keywords.addAll(copy.getKeywords());
        keywords.add(keyword);
        copy.setKeywords(Set.copyOf(keywords));
        copy.freeze();
        return copy;
    }
}
