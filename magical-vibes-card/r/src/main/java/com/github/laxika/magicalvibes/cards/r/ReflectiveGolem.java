package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsSingleTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "STX", collectorNumber = "257")
public class ReflectiveGolem extends Card {

    public ReflectiveGolem() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));

        addTrigger(Triggers.youCastSpell()
                        .withFilter(instantOrSorcery)
                        .withStackEntryFilter(new StackEntryAllOfPredicate(List.of(
                                new StackEntryIsSingleTargetPredicate(),
                                new StackEntryTargetsSourcePredicate()))),
                new MayPayManaEffect("{2}", new CopyTriggeringSpellEffect(), "Pay {2} to copy that spell?"));
    }
}
