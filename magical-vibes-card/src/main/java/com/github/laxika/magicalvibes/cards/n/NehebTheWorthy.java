package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "AKH", collectorNumber = "203")
public class NehebTheWorthy extends Card {

    public NehebTheWorthy() {
        // Other Minotaurs you control have first strike. (OWN_CREATURES excludes this source = "other".)
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.OWN_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.MINOTAUR)));

        // As long as you have one or fewer cards in hand, Minotaurs you control get +2/+0.
        // ALL_OWN_CREATURES because this creature is itself a Minotaur and qualifies.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new CardsInHandAtMost(1),
                new StaticBoostEffect(2, 0, GrantScope.ALL_OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.MINOTAUR))));

        // Whenever Neheb deals combat damage to a player, each player discards a card.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DiscardEffect(1, DiscardRecipient.EACH_PLAYER));
    }
}
