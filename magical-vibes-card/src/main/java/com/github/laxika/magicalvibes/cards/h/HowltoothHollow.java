package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.NoPlayerHasCardsInHand;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayImprintedCardWithoutPayingManaCostEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "269")
public class HowltoothHollow extends Card {

    public HowltoothHollow() {
        // Hideaway 4 — when this enters, look at the top four cards, exile one face down, rest on bottom.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ImprintFromTopCardsEffect(4));
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        // {T}: Add {B}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLACK));
        // {B}, {T}: You may play the exiled card without paying its mana cost if each player has no cards in hand.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(new ConditionalEffect(new NoPlayerHasCardsInHand(),
                        new PlayImprintedCardWithoutPayingManaCostEffect())),
                "{B}, {T}: You may play the exiled card without paying its mana cost if each player has no cards in hand."
        ));
    }
}
