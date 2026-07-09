package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsHandThenDrawsThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "179")
public class IncendiaryCommand extends Card {

    public IncendiaryCommand() {
        // Choose two — each targeting mode declares its own per-mode target filter, so the choose-two
        // unwrap gives each chosen mode its own target slot (mode 0 = player/planeswalker, mode 2 =
        // nonbasic land). Modes 1 and 3 are non-targeting.
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Incendiary Command deals 4 damage to target player or planeswalker",
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(4),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsPlaneswalkerPredicate(),
                                "Target must be a player or planeswalker."
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Incendiary Command deals 2 damage to each creature",
                        new MassDamageEffect(2)),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy target nonbasic land",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsLandPredicate(),
                                        new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC))
                                )),
                                "Target must be a nonbasic land."
                        )
                ),
                new ChooseOneEffect.ChooseOneOption(
                        "Each player discards all the cards in their hand, then draws that many cards",
                        new EachPlayerDiscardsHandThenDrawsThatManyEffect())
        ), 2));
    }
}
