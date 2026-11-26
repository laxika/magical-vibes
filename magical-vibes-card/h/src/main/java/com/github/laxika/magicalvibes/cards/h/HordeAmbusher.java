package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "110")
public class HordeAmbusher extends Card {

    public HordeAmbusher() {
        addMorphWithRevealCost(new CardColorPredicate(CardColor.RED), "red");
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE,
                DealDamageEffect.to(Subjects.you(), 1));
        target(TargetFilters.creature()).addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new CantBlockThisTurnEffect(Subjects.target()));
    }
}
