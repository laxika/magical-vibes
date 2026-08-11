package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "58")
public class DroolingOgre extends Card {

    public DroolingOgre() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.ARTIFACT),
                List.of(new TargetPlayerGainsControlOfSourceCreatureEffect())
        ));
    }
}
