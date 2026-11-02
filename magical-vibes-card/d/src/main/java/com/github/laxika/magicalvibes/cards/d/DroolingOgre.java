package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "58")
public class DroolingOgre extends Card {

    public DroolingOgre() {
        addTrigger(Triggers.playerCastsSpell(), new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.ARTIFACT),
                List.of(TargetPlayerGainsControlOfSourceCreatureEffect.triggeringPlayer())
        ));
    }
}
