package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCopyTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "213")
public class PrismariApprentice extends Card {

    public PrismariApprentice() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        // Magecraft: Prismari Apprentice can't be blocked this turn. If that spell's mana value is
        // 5 or greater, put a +1/+1 counter on it.
        List<CardEffect> magecraft = List.of(
                new MakeCreatureUnblockableEffect(true),
                new ConditionalEffect(new EventValueAtLeast(5), new PutCountersOnSourceEffect(1, 1, 1)));

        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(instantOrSorcery, magecraft));
        addTrigger(Triggers.youCopySpell(), new SpellCopyTriggerEffect(instantOrSorcery, magecraft));
    }
}
