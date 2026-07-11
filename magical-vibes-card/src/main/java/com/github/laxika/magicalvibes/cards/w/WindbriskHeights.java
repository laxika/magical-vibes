package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesThisTurn;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayImprintedCardWithoutPayingManaCostEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "281")
public class WindbriskHeights extends Card {

    public WindbriskHeights() {
        // Hideaway 4 — when this enters, look at the top four cards, exile one face down, rest on bottom.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ImprintFromTopCardsEffect(4));
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        // {T}: Add {W}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.WHITE));
        // {W}, {T}: You may play the exiled card without paying its mana cost if you attacked with three or more creatures this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(new ConditionalEffect(new AttackedWithCreaturesThisTurn(3),
                        new PlayImprintedCardWithoutPayingManaCostEffect())),
                "{W}, {T}: You may play the exiled card without paying its mana cost if you attacked with three or more creatures this turn."
        ));
    }
}
