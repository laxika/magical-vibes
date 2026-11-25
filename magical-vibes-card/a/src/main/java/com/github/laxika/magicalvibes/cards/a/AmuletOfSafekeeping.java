package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "226")
public class AmuletOfSafekeeping extends Card {

    public AmuletOfSafekeeping() {
        addTrigger(Triggers.youBecomeTargetOfOpponentSpellOrAbility(),
                UnlessPaysEffect.counterUnlessPays(1));
        addStaticEffect(new StaticBoostEffect(-1, 0, Subjects.otherCreatures(),
                new PermanentIsTokenPredicate()));
    }
}
