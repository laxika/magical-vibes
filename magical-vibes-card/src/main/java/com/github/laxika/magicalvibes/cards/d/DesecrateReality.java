package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueParityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueParityPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import java.util.List;

@CardRegistration(set = "CMM", collectorNumber = "714")
@CardRegistration(set = "CMM", collectorNumber = "746")
public class DesecrateReality extends Card {

    public DesecrateReality() {
        PermanentAllOfPredicate evenPermanentOpponentControls = new PermanentAllOfPredicate(List.of(
                new PermanentManaValueParityPredicate(ManaValueParity.EVEN),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));

        setMultiTargetConstraint(MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE);
        target(new PermanentPredicateTargetFilter(
                evenPermanentOpponentControls,
                "Target must be an even-mana-value permanent an opponent controls"), 0, 99)
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());

        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.COLORLESS, 3),
                new ReturnCardsFromControllerGraveyardToBattlefieldEffect(
                        new CardAllOfPredicate(List.of(
                                new CardIsPermanentPredicate(),
                                new CardManaValueParityPredicate(ManaValueParity.ODD))),
                        new Fixed(1),
                        false, null, true, false)));
    }
}
