package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextCombatDamageFromSourceToControllerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SCG", collectorNumber = "95")
public class GoblinPsychopath extends Card {

    public GoblinPsychopath() {
        CardEffect effect = new FlipCoinWinEffect(null,
                new RedirectNextCombatDamageFromSourceToControllerEffect());
        addTrigger(List.of(Triggers.attacks(), Triggers.blocks()), effect);
    }
}
