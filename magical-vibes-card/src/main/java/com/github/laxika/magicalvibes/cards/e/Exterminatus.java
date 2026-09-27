package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "120")
public class Exterminatus extends Card {

    public Exterminatus() {
        var opponentsNonlandPermanents = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));
        var nonlandPermanents = new PermanentNotPredicate(new PermanentIsLandPredicate());

        addEffect(EffectSlot.SPELL,
                new RemoveKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ALL_PERMANENTS,
                        opponentsNonlandPermanents));
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(nonlandPermanents));
    }
}
