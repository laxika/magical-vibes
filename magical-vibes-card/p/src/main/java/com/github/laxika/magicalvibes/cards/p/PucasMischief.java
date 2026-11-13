package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "47")
@CardRegistration(set = "SLD", collectorNumber = "923")
public class PucasMischief extends Card {

    public PucasMischief() {
        PermanentNotPredicate nonland = new PermanentNotPredicate(new PermanentIsLandPredicate());
        setMultiTargetConstraint(MultiTargetConstraint.MANA_VALUE_AT_MOST_FIRST_TARGET);
        target(new ControlledPermanentPredicateTargetFilter(nonland,
                "Target must be a nonland permanent you control"));
        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                        new ExchangeControlOfTargetPermanentsEffect(nonland, true),
                        "You may exchange control of target nonland permanent you control and target nonland "
                                + "permanent an opponent controls with equal or lesser mana value."));
    }
}
