package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "233")
public class SilverquillPledgemage extends Card {

    public SilverquillPledgemage() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        List<CardEffect> magecraft = List.of(new GrantChosenKeywordEffect(
                List.of(Keyword.FLYING, Keyword.LIFELINK), GrantScope.SELF));

        addTrigger(Triggers.youCastSpell().withFilter(instantOrSorcery), magecraft.toArray(CardEffect[]::new));
        addTrigger(Triggers.youCopySpell().withFilter(instantOrSorcery), magecraft.toArray(CardEffect[]::new));
    }
}
