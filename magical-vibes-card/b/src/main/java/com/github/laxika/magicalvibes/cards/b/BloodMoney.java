package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

@CardRegistration(set = "SLD", collectorNumber = "1789")
@CardRegistration(set = "LCC", collectorNumber = "183")
@CardRegistration(set = "HBG", collectorNumber = "146")
public class BloodMoney extends Card {

    public BloodMoney() {
        addSpellEffect(new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
        addSpellEffect(CreateTokenEffect.ofTappedTreasureToken(new AmountThisWay(StepKind.DESTROY)
                .matching(new PermanentNotPredicate(new PermanentIsTokenPredicate()))));
    }
}
