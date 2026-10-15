package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsModifiedPredicate;

@CardRegistration(set = "YNEO", collectorNumber = "18")
public class ArtilleryEnthusiast extends Card {

    public ArtilleryEnthusiast() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.MENACE,
                GrantScope.ALL_OWN_CREATURES,
                new PermanentIsModifiedPredicate()));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                DiscardCardThenEffect.continuing(
                        null,
                        new SeekCardsToHandEffect(
                                new Fixed(1),
                                null,
                                new ManaValueBound(new LastDiscardedCardManaValue(), true, 0)),
                        "a card"),
                "Discard a card to seek a card with the same mana value?"));
    }
}
