package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_OWN),
                new TriggeringCardConditionalEffect(new CardNotPredicate(new CardIsTokenPredicate()),
                        CreateTokenEffect.whiteSpirit(1)));
    }
}
