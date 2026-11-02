package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "78")
public class GladeGnarr extends Card {

    public GladeGnarr() {
        addTrigger(Triggers.playerCastsSpell(),
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.BLUE),
                        List.of(new BoostSelfEffect(2, 2))));
    }
}
