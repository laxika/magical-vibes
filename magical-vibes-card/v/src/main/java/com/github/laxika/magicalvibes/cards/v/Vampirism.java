package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "73")
@CardRegistration(set = "MGB", collectorNumber = "6")
public class Vampirism extends Card {

    public Vampirism() {
        // Enchant creature
        target(TargetFilters.creature());

        // When this Aura enters, draw a card at the beginning of the next turn's upkeep.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterDrawCardsAtNextUpkeepEffect());

        PermanentCount otherCreaturesYouControl = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentIsHostOfSourceAuraPredicate())
                )),
                CountScope.CONTROLLER);

        // Enchanted creature gets +1/+1 for each other creature you control.
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                otherCreaturesYouControl, otherCreaturesYouControl, GrantScope.ENCHANTED_CREATURE));

        // Other creatures you control get -1/-1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, -1, GrantScope.OWN_CREATURES,
                new PermanentNotPredicate(new PermanentIsHostOfSourceAuraPredicate())));
    }
}
