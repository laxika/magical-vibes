package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForEachDestroyedPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "115")
@CardRegistration(set = "PD3", collectorNumber = "7")
@CardRegistration(set = "SLD", collectorNumber = "1318")
@CardRegistration(set = "2XM", collectorNumber = "182")
@CardRegistration(set = "C14", collectorNumber = "218")
@CardRegistration(set = "C15", collectorNumber = "204")
public class Terastodon extends Card {

    public Terastodon() {
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                "Target must be a noncreature permanent"), 0, 3);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyEachTargetPermanentEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenForEachDestroyedPermanentControllerEffect(
                        new CreateTokenEffect("Elephant", 3, 3, CardColor.GREEN,
                                List.of(CardSubtype.ELEPHANT), Set.of(), Set.of())));
    }
}
