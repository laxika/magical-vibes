package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardThenMayPutPermanentWithManaValueAtMostLandsEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "1")
public class AnticausalVestige extends Card {

    public AnticausalVestige() {
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new DrawCardThenMayPutPermanentWithManaValueAtMostLandsEffect());
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}"))));
    }
}
