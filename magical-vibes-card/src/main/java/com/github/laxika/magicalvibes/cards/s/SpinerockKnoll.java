package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.OpponentDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayImprintedCardWithoutPayingManaCostEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "274")
public class SpinerockKnoll extends Card {

    public SpinerockKnoll() {
        // Hideaway 4 — when this enters, look at the top four cards, exile one face down, rest on bottom.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ImprintFromTopCardsEffect(4));
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        // {T}: Add {R}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.RED));
        // {R}, {T}: You may play the exiled card without paying its mana cost if an opponent was dealt 7 or more damage this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new ConditionalEffect(new OpponentDealtDamageThisTurn(7),
                        new PlayImprintedCardWithoutPayingManaCostEffect())),
                "{R}, {T}: You may play the exiled card without paying its mana cost if an opponent was dealt 7 or more damage this turn."
        ));
    }
}
