package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AirbendTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "81")
@CardRegistration(set = "TLE", collectorNumber = "173")
public class MonkGyatso extends Card {

    public MonkGyatso() {
        addTrigger(Triggers.anotherCreatureYouControlBecomesTargetOfSpellOrAbility(),
                new MayEffect(new AirbendTargetPermanentEffect(new PermanentIsCreaturePredicate()), "Airbend that creature?"));
    }
}
