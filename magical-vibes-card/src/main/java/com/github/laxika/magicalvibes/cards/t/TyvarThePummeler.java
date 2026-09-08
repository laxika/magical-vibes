package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "202")
public class TyvarThePummeler extends Card {

    public TyvarThePummeler() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new TapCreatureCost(new PermanentIsCreaturePredicate(), true, false),
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF),
                        new TapPermanentsEffect(TapUntapScope.SELF)),
                "Tap another untapped creature you control: Tyvar, the Pummeler gains indestructible until end of turn. Tap it."));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}{G}",
                List.of(new BoostAllOwnCreaturesEffect(
                        new GreatestPowerAmongControlled(), new GreatestPowerAmongControlled())),
                "{3}{G}{G}: Creatures you control get +X/+X until end of turn, where X is the greatest power among creatures you control."));
    }
}
