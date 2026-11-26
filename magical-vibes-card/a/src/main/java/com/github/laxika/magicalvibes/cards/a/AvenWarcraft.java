package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "JUD", collectorNumber = "2")
public class AvenWarcraft extends Card {

    public AvenWarcraft() {
        addSpellEffect(BoostEffect.creaturesYouControl(0, 2));
        addSpellEffect(new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new GrantProtectionChoiceUntilEndOfTurnEffect(Subjects.creaturesYouControl())));
    }
}
