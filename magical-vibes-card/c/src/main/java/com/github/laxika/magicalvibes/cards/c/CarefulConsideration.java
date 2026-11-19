package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CastDuringMainPhase;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "TSP", collectorNumber = "52")
@CardRegistration(set = "MMA", collectorNumber = "37")
@CardRegistration(set = "TSR", collectorNumber = "56")
public class CarefulConsideration extends Card {

    public CarefulConsideration() {
        addSpellEffect(new DrawCardEffect(4, Subjects.targetPlayerDeclaredElsewhere()));
        addSpellEffect(new ConditionalEffect(
                new CastDuringMainPhase(), new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER)));
        addSpellEffect(new ConditionalEffect(
                new NotCondition(new CastDuringMainPhase()), new DiscardEffect(3, DiscardRecipient.TARGET_PLAYER)));
    }
}
