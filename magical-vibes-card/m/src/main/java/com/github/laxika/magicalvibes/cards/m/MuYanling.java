package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GS1", collectorNumber = "1")
public class MuYanling extends Card {

    public MuYanling() {
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new MakeCreatureUnblockableEffect()),
                "+2: Target creature can't be blocked this turn.",
                TargetFilters.creature()));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DrawCardEffect(2)),
                "−3: Draw two cards."));

        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(
                        new TapPermanentsEffect(TapUntapScope.ALL_CREATURES,
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())),
                        new ControllerExtraTurnEffect(1)),
                "−10: Tap all creatures your opponents control. You take an extra turn after this one."));
    }
}
