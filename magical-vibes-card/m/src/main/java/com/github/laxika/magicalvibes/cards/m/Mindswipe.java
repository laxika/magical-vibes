package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "KTK", collectorNumber = "189")
public class Mindswipe extends Card {

    public Mindswipe() {
        // Counter target spell unless its controller pays {X}. Mindswipe deals X damage to that spell's controller.
        addSpellEffect(UnlessPaysEffect.counterUnlessPaysX());
        addSpellEffect(new DealDamageToPlayersEffect(
                new XValue(), Subjects.controllerOf(Subjects.targetSpell())));
    }
}
