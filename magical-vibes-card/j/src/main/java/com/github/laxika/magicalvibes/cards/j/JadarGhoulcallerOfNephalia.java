package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "108")
@CardRegistration(set = "SOC", collectorNumber = "217")
@CardRegistration(set = "DBL", collectorNumber = "108")
public class JadarGhoulcallerOfNephalia extends Card {

    public JadarGhoulcallerOfNephalia() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new NotCondition(new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasKeywordPredicate(Keyword.DECAYED)))))),
                CreateTokenEffect.blackZombieWithDecayed(1));
    }
}
