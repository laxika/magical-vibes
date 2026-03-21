package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "245")
public class MemorialToUnity extends Card {

    public MemorialToUnity() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN));
        // {2}{G}, {T}, Sacrifice Memorial to Unity: Look at the top five cards of your library.
        // You may reveal a creature card from among them and put it into your hand.
        // Put the rest on the bottom of your library in a random order.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{G}",
                List.of(new SacrificeSelfCost(),
                        new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(5, new CardTypePredicate(CardType.CREATURE))),
                "{2}{G}, {T}, Sacrifice Memorial to Unity: Look at the top five cards of your library. You may reveal a creature card from among them and put it into your hand. Put the rest on the bottom of your library in a random order."
        ));
    }
}
