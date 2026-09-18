package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerCreatesTokensEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "160")
@CardRegistration(set = "FDN", collectorNumber = "311")
@CardRegistration(set = "SNC", collectorNumber = "51")
@CardRegistration(set = "SLZ", collectorNumber = "27")
@CardRegistration(set = "SLZ", collectorNumber = "148")
@CardRegistration(set = "SLZ", collectorNumber = "269")
public class AnOfferYouCantRefuse extends Card {

    public AnOfferYouCantRefuse() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))
                ),
                "Target must be a noncreature spell."
        ))
                .addEffect(EffectSlot.SPELL,
                        new TargetSpellControllerCreatesTokensEffect(CreateTokenEffect.ofTreasureToken(2)))
                .addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
