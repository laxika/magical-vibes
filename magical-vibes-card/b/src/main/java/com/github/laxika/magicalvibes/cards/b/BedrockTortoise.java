package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageWithToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessGreaterThanPowerPredicate;

@CardRegistration(set = "LCI", collectorNumber = "176")
@CardRegistration(set = "LCI", collectorNumber = "378")
public class BedrockTortoise extends Card {

    public BedrockTortoise() {
        addStaticEffect(new ConditionalEffect(new ControllerTurn(),
                new GrantKeywordEffect(Keyword.HEXPROOF, Subjects.creaturesYouControl())));
        addStaticEffect(new AssignCombatDamageWithToughnessEffect(
                Subjects.creaturesYouControl(), new PermanentToughnessGreaterThanPowerPredicate()));
    }
}
