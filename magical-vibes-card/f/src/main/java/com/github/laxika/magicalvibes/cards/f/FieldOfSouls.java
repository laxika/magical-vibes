package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "18")
@CardRegistration(set = "TPR", collectorNumber = "13")
@CardRegistration(set = "DDK", collectorNumber = "30")
@CardRegistration(set = "EMA", collectorNumber = "11")
@CardRegistration(set = "VOC", collectorNumber = "86")
public class FieldOfSouls extends Card {

    public FieldOfSouls() {
        // Whenever a nontoken creature is put into your graveyard from the battlefield,
        // create a 1/1 white Spirit creature token with flying. Keyed to the graveyard owner, not
        // the creature's last controller.
        addTrigger(Triggers.dies(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        ObjectRelation.YOU_OWN),
                CreateTokenEffect.whiteSpirit(1));
    }
}
