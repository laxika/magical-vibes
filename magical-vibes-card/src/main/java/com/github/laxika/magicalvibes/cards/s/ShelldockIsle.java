package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.AnyLibraryAtMost;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintFromTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayImprintedCardWithoutPayingManaCostEffect;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "272")
public class ShelldockIsle extends Card {

    public ShelldockIsle() {
        // Hideaway 4 — when this enters, look at the top four cards, exile one face down, rest on bottom.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ImprintFromTopCardsEffect(4));
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        // {T}: Add {U}.
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLUE));
        // {U}, {T}: You may play the exiled card without paying its mana cost if a library has twenty or fewer cards in it.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new ConditionalEffect(new AnyLibraryAtMost(20),
                        new PlayImprintedCardWithoutPayingManaCostEffect())),
                "{U}, {T}: You may play the exiled card without paying its mana cost if a library has twenty or fewer cards in it."
        ));
    }
}
