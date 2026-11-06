package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "ECL", collectorNumber = "159")
@CardRegistration(set = "ECL", collectorNumber = "322")
@CardRegistration(set = "ECL", collectorNumber = "399")
public class SpinerockTyrant extends Card {

    public SpinerockTyrant() {
        addTrigger(Triggers.youCastSpell()
                        .withFilter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))))
                        .withStackEntryFilter(new StackEntryIsSingleTargetPredicate()),
                new MayEffect(new CopyTriggeringSpellEffect().withGrantedKeywords(Set.of(Keyword.WITHER)), "Copy that spell?"));
    }
}
