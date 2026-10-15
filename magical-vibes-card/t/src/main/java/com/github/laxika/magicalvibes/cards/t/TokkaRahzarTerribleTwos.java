package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaSpentLessThanManaValuePredicate;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "171")
@CardRegistration(set = "TMT", collectorNumber = "252")
public class TokkaRahzarTerribleTwos extends Card {

    public TokkaRahzarTerribleTwos() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new SpellCastTriggerEffect(
                        null,
                        List.of(new DealDamageToPlayersEffect(3, DamageRecipient.TRIGGERING_PLAYER)),
                        new StackEntryManaSpentLessThanManaValuePredicate()));
    }
}
