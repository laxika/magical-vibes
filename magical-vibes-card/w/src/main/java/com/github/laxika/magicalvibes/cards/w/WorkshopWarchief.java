package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "165")
public class WorkshopWarchief extends Card {

    public WorkshopWarchief() {
        addCastingOption(AlternateHandCast.blitz("{4}{G}{G}"));

        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenEffect("Rhino Warrior", 4, 4, CardColor.GREEN,
                List.of(CardSubtype.RHINO, CardSubtype.WARRIOR), Set.of(), Set.of()));
    }
}
