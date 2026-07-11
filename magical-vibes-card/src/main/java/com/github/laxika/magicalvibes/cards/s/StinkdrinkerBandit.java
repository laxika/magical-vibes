package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOR", collectorNumber = "80")
public class StinkdrinkerBandit extends Card {

    public StinkdrinkerBandit() {
        // Prowl {1}{B}: cast for this cost if you dealt combat damage to a player this turn with a Goblin or Rogue.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{B}")),
                Set.of(CardSubtype.GOBLIN, CardSubtype.ROGUE)));

        // Whenever a Rogue you control attacks and isn't blocked, it gets +2/+1 until end of turn.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS_UNBLOCKED,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.ROGUE),
                        new BoostSelfEffect(2, 1)));
    }
}
