package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCumulativeUpkeepPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "59")
public class BalduvianShaman extends Card {

    public BalduvianShaman() {
        // {T}: Change the text of target white enchantment you control that doesn't have cumulative
        // upkeep by replacing all instances of one color word with another. That enchantment gains
        // "Cumulative upkeep {1}."
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new ChangeColorTextEffect(true, false, false),
                        new GrantEffectToTargetEffect(
                                EffectSlot.UPKEEP_TRIGGERED,
                                new CumulativeUpkeepEffect("{1}"))
                ),
                "{T}: Change the text of target white enchantment you control that doesn't have "
                        + "cumulative upkeep by replacing all instances of one color word with another. "
                        + "That enchantment gains \"Cumulative upkeep {1}.\"",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsEnchantmentPredicate(),
                                new PermanentColorInPredicate(Set.of(CardColor.WHITE)),
                                new PermanentNotPredicate(new PermanentHasCumulativeUpkeepPredicate())
                        )),
                        "Target must be a white enchantment you control that doesn't have cumulative upkeep"
                )
        ));
    }
}
