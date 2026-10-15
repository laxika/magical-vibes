package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesByColorsSpentOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "123")
public class MagmabloodArchaic extends Card {

    public MagmabloodArchaic() {
        // Converge — This creature enters with a +1/+1 counter on it for each color of mana spent
        // to cast it. Converge is loaded from Scryfall and supplies the stack entry's X value.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));

        // Whenever you cast an instant or sorcery spell, creatures you control get +1/+0 until end
        // of turn for each color of mana spent to cast that spell.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new BoostAllOwnCreaturesByColorsSpentOnSpellCastEffect(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                ))));
    }
}
