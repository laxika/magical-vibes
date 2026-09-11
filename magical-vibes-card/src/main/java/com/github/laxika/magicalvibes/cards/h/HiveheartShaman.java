package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardDoesNotShareLandTypeWithControlledLandPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "VOW", collectorNumber = "202")
public class HiveheartShaman extends Card {

    public HiveheartShaman() {
        CardAllOfPredicate basicLandWithUniqueType = new CardAllOfPredicate(List.of(
                CardPredicateUtils.basicLand(),
                new CardDoesNotShareLandTypeWithControlledLandPredicate()));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SearchLibraryEffect(basicLandWithUniqueType, LibrarySearchDestination.BATTLEFIELD),
                "Search your library for a basic land card that doesn't share a land type with a land you control?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}",
                List.of(new CreateXTokenWithXCountersEffect(
                        new CreateTokenEffect("Insect", 1, 1, CardColor.GREEN,
                                List.of(CardSubtype.INSECT), Set.of(), Set.of()),
                        new BasicLandTypesAmongControlledLands(),
                        CounterType.PLUS_ONE_PLUS_ONE)),
                "{5}{G}: Create a 1/1 green Insect creature token. Put X +1/+1 counters on it, where X is the number of basic land types among lands you control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
