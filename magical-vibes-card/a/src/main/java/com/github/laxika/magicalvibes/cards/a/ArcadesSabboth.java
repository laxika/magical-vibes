package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "CHR", collectorNumber = "71")
@CardRegistration(set = "LEG", collectorNumber = "218")
@CardRegistration(set = "ME3", collectorNumber = "142")
@CardRegistration(set = "SLD", collectorNumber = "1067")
@CardRegistration(set = "DMR", collectorNumber = "187")
public class ArcadesSabboth extends Card {

    public ArcadesSabboth() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                UnlessPaysEffect.unlessYouPay("{G}{W}{U}", new SacrificeSelfEffect()));

        addStaticEffect(new StaticBoostEffect(
                0,
                2,
                Subjects.creaturesYouControl(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsTappedPredicate()),
                        new PermanentNotPredicate(new PermanentIsAttackingPredicate())
                ))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new BoostSelfEffect(0, 1)),
                "{W}: This creature gets +0/+1 until end of turn."));
    }
}
