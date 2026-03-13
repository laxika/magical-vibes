package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedWishCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeOrExileEffect;

import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "50")
public class DjinnOfWishes extends Card {

    public DjinnOfWishes() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedWishCountersEffect(3));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}{U}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.WISH),
                        new RevealTopCardMayPlayFreeOrExileEffect()
                ),
                "Reveal the top card of your library. You may play that card without paying its mana cost. If you don't, exile it."
        ));
    }
}
