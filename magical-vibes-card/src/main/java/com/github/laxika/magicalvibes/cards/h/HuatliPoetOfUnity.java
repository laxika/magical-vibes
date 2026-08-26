package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RoarOfTheFifthPeople;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "189")
public class HuatliPoetOfUnity extends Card {

    public HuatliPoetOfUnity() {
        setBackFaceCard(new RoarOfTheFifthPeople());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                        new CardSupertypePredicate(CardSupertype.BASIC),
                        new CardTypePredicate(CardType.LAND)
                ))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R/W}{R/W}",
                List.of(new ExileSelfAndReturnTransformedEffect()),
                "{3}{R/W}{R/W}: Exile Huatli, then return her to the battlefield transformed under her owner's control. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "RoarOfTheFifthPeople";
    }
}
