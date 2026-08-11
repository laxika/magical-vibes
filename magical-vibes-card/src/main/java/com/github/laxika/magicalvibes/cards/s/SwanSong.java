package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "THS", collectorNumber = "65")
public class SwanSong extends Card {

    public SwanSong() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.ENCHANTMENT_SPELL,
                        StackEntryType.INSTANT_SPELL,
                        StackEntryType.SORCERY_SPELL
                )),
                "Target must be an enchantment, instant, or sorcery spell."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect())
                .addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                        "Bird", 2, 2, CardColor.BLUE,
                        List.of(CardSubtype.BIRD), Set.of(Keyword.FLYING), Set.of()));
    }
}
