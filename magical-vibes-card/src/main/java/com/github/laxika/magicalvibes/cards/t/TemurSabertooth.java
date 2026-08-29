package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnUpToNTargetPermanentsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "141")
public class TemurSabertooth extends Card {

    public TemurSabertooth() {
        addActivatedAbility(new ActivatedAbility(false, "{1}{G}",
                List.of(new ReturnUpToNTargetPermanentsToHandEffect(1,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentControlledBySourceControllerPredicate(),
                                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                                )),
                                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF))),
                "{1}{G}: You may return another creature you control to its owner's hand. If you do, this creature gains indestructible until end of turn."));
    }
}
