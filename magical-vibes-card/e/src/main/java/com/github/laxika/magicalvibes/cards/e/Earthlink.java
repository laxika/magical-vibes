package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "ICE", collectorNumber = "285")
@CardRegistration(set = "ME2", collectorNumber = "192")
public class Earthlink extends Card {

    public Earthlink() {
        // At the beginning of your upkeep, sacrifice this enchantment unless you pay {2}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{2}", new SacrificeSelfEffect()));

        // Whenever a creature dies, that creature's controller sacrifices a land of their choice.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(),
                        Subjects.controllerOf(Subjects.triggering())));
    }
}
