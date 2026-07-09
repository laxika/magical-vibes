package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "197")
public class BattlewandOak extends Card {

    public BattlewandOak() {
        // Whenever a Forest you control enters, this creature gets +2/+2 until end of turn.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.FOREST),
                        new BoostSelfEffect(2, 2)));

        // Whenever you cast a Treefolk spell, this creature gets +2/+2 until end of turn.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardSubtypePredicate(CardSubtype.TREEFOLK),
                List.of(new BoostSelfEffect(2, 2))));
    }
}
