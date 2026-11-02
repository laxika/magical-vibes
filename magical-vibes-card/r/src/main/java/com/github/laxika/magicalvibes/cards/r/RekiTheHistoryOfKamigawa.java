package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOK", collectorNumber = "142")
@CardRegistration(set = "SLD", collectorNumber = "263")
public class RekiTheHistoryOfKamigawa extends Card {

    public RekiTheHistoryOfKamigawa() {
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardSupertypePredicate(CardSupertype.LEGENDARY),
                List.of(new DrawCardEffect())
        ));
    }
}
