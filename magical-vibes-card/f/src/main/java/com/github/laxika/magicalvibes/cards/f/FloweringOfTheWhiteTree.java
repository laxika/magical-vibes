package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOC", collectorNumber = "14")
@CardRegistration(set = "HOC", collectorNumber = "54")
public class FloweringOfTheWhiteTree extends Card {

    public FloweringOfTheWhiteTree() {
        PermanentHasSupertypePredicate legendary = new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY);

        addStaticEffect(new StaticBoostEffect(2, 1, Subjects.creaturesYouControl(), legendary));
        addStaticEffect(new StaticBoostEffect(1, 1, Subjects.creaturesYouControl(),
                new PermanentNotPredicate(legendary)));
        addStaticEffect(new GrantTriggeredAbilityEffect(
                new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                UnlessPaysEffect.counterUnlessPays(1)),
                Subjects.creaturesYouControl(),
                legendary));
    }
}
