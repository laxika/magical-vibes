package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasExhaustAbilityPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDFT", collectorNumber = "25")
public class SalaDeckBoss extends Card {

    public SalaDeckBoss() {
        addStaticEffect(new GrantKeywordEffect(
                Keyword.HASTE,
                Subjects.creaturesYouControl(),
                new PermanentHasExhaustAbilityPredicate()));

        addTrigger(Triggers.youActivateNonManaAbility().withKind(AbilityTrigger.AbilityKind.EXHAUST),
                new CopyTriggeringSpellOrAbilityEffect());
    }
}
