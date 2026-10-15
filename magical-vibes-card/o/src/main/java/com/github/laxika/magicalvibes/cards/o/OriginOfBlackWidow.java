package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEqualToCardsInTheirGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "MSC", collectorNumber = "664")
public class OriginOfBlackWidow extends Card {

    public OriginOfBlackWidow() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new EachOpponentLosesLifeEqualToCardsInTheirGraveyardEffect(
                        new CardTypePredicate(CardType.CREATURE)));
    }
}
