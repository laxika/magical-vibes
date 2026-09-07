package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextSpellCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "142")
public class JeongJeongTheDeserter extends Card {

    public JeongJeongTheDeserter() {
        addEffect(EffectSlot.ON_ATTACK, new AwardManaUntilEndOfCombatEffect(ManaColor.RED, 1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new CopyNextSpellCastThisTurnEffect(
                                new CardSubtypePredicate(CardSubtype.LESSON), Set.of())
                ),
                "Exhaust — {3}: Put a +1/+1 counter on Jeong Jeong. When you next cast a Lesson spell this turn, copy it."
                        + " You may choose new targets for the copy. (Activate each exhaust ability only once.)"
        ).withMaxActivationsPerGame(1).withExhaust());
    }
}
