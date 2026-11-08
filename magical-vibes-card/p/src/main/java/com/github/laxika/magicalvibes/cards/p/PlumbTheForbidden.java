package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellXValue;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STX", collectorNumber = "81")
@CardRegistration(set = "SOC", collectorNumber = "224")
@CardRegistration(set = "BLC", collectorNumber = "188")
public class PlumbTheForbidden extends Card {

    public PlumbTheForbidden() {
        addTrigger(Triggers.youCastThisSpell(),
                new CopyTriggeringSpellEffect().withCopies(new TriggeringSpellXValue()));
        addCastingCost(new SacrificeAnyNumberOfPermanentsCost(new PermanentIsCreaturePredicate()));
        addSpellEffect(new DrawCardEffect());
        addSpellEffect(new LoseLifeEffect(1));
    }
}
