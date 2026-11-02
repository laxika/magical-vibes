package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "61")
public class DwarvenPatrol extends Card {

    public DwarvenPatrol() {
        addStaticEffect(DoesntUntapEffect.self());
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardNotPredicate(new CardColorPredicate(CardColor.RED)),
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF))));
    }
}
