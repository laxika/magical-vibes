package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachOpponentLibraryAndAllowOneMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "104")
@CardRegistration(set = "TLE", collectorNumber = "186")
public class FireLordOzai extends Card {

    public FireLordOzai() {
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new AwardManaUntilEndOfCombatEffect(
                                ManaColor.RED, AmountThisWay.sacrificedPower(), false))),
                "Sacrifice another creature?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}",
                List.of(new ExileTopCardOfEachOpponentLibraryAndAllowOneMayPlayThisTurnEffect()),
                "{6}: Exile the top card of each opponent's library. Until end of turn, you may play one of those cards without paying its mana cost."
        ));
    }
}
