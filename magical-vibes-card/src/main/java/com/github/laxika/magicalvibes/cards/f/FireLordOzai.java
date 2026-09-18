package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachOpponentLibraryAndAllowOneMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.amount.SacrificedPermanentPower;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "104")
@CardRegistration(set = "TLE", collectorNumber = "186")
public class FireLordOzai extends Card {

    public FireLordOzai() {
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SacrificePermanentThenEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )),
                        new AwardManaUntilEndOfCombatEffect(
                                ManaColor.RED, new SacrificedPermanentPower()),
                        "another creature"),
                "Sacrifice another creature?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}",
                List.of(new ExileTopCardOfEachOpponentLibraryAndAllowOneMayPlayThisTurnEffect()),
                "{6}: Exile the top card of each opponent's library. Until end of turn, you may play one of those cards without paying its mana cost."
        ));
    }
}
