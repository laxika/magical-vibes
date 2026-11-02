package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "VIS", collectorNumber = "104")
public class ElephantGrass extends Card {

    public ElephantGrass() {
        // Cumulative upkeep {1}
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{1}"));

        // Black creatures can't attack you — exemption is nonblack, so black is hard-denied.
        addStaticEffect(new CreaturesCantAttackControllerUnlessPredicateEffect(
                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK)))));

        // Nonblack creatures can't attack you unless their controller pays {2} for each.
        // Uniform tax from the defender; black attackers are already barred above.
        addStaticEffect(RequirePaymentToAttackEffect.playerOnly(2));
    }
}
