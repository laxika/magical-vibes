package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetSpellMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntrySubtypeInPredicate;

import java.util.Set;

@CardRegistration(set = "HOU", collectorNumber = "38")
public class JacesDefeat extends Card {

    public JacesDefeat() {
        // "blue" is a targeting restriction: may only target a blue spell.
        target(new StackEntryPredicateTargetFilter(
                new StackEntryColorInPredicate(Set.of(CardColor.BLUE)),
                "Target spell must be blue."));
        // Scry check reads the countered spell while it is still on the stack, so it must
        // resolve before the counter removes it (Jace planeswalkers all carry the Jace subtype).
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new TargetSpellMatches(new StackEntrySubtypeInPredicate(Set.of(CardSubtype.JACE))),
                new ScryEffect(2)));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
