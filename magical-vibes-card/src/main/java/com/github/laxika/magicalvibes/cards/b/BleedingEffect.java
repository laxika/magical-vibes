package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "51")
public class BleedingEffect extends Card {

    private static final List<Keyword> SHARED_KEYWORDS = List.of(
            Keyword.FLYING,
            Keyword.FIRST_STRIKE,
            Keyword.DOUBLE_STRIKE,
            Keyword.DEATHTOUCH,
            Keyword.HEXPROOF,
            Keyword.INDESTRUCTIBLE,
            Keyword.LIFELINK,
            Keyword.MENACE,
            Keyword.REACH,
            Keyword.TRAMPLE,
            Keyword.VIGILANCE
    );

    public BleedingEffect() {
        // At the beginning of combat on your turn, creatures you control gain each watched
        // keyword until end of turn if a creature card in your graveyard has that keyword.
        // These trailing "if" clauses are resolution-time conditions, not intervening-if gates.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                SHARED_KEYWORDS.stream()
                        .map(BleedingEffect::grantKeywordFromGraveyard)
                        .toArray(CardEffect[]::new)));
    }

    private static CardEffect grantKeywordFromGraveyard(Keyword keyword) {
        return ConditionalEffect.unless(
                new GraveyardCardThreshold(1, new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardKeywordPredicate(keyword)
                ))),
                new GrantKeywordEffect(keyword, GrantScope.ALL_OWN_CREATURES));
    }
}
