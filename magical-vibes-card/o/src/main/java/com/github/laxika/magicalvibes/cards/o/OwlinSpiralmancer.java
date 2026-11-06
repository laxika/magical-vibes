package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;

@CardRegistration(set = "SOC", collectorNumber = "22")
@CardRegistration(set = "SOC", collectorNumber = "72")
public class OwlinSpiralmancer extends Card {

    public OwlinSpiralmancer() {
        CardHasXInManaCostPredicate xSpell = new CardHasXInManaCostPredicate();
        addTrigger(Triggers.youCastSpell().withFilter(xSpell).nthThisTurn(1),
                new MayEffect(new CopyTriggeringSpellEffect(true), "Copy that spell?"));
    }
}
