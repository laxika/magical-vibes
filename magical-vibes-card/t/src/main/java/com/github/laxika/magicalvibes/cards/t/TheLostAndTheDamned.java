package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PlayFromOutsideHandTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredOrCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "129")
public class TheLostAndTheDamned extends Card {

    public TheLostAndTheDamned() {
        CreateTokenEffect spawn = new CreateTokenEffect(
                "Spawn", 3, 3, CardColor.RED, List.of(CardSubtype.SPAWN), Set.of(), Set.of());
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.LAND,
                                new PermanentNotPredicate(new PermanentEnteredOrCastFromZonePredicate(Zone.HAND)))),
                        ObjectRelation.YOU_CONTROL),
                spawn);
        addTrigger(Triggers.youCastSpell(),
                new PlayFromOutsideHandTriggerEffect(List.of(spawn)));
    }
}
