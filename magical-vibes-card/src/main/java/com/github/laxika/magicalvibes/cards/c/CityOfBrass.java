package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "322")
public class CityOfBrass extends Card {

    public CityOfBrass() {
        // Whenever this land becomes tapped, it deals 1 damage to you. The conditional restricts the
        // shared tap trigger to the source itself. When it taps for mana the trigger is deferred until
        // a player next receives priority (CR 603.3).
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED, new TriggeringPermanentConditionalEffect(
                new PermanentIsSourceCardPredicate(),
                new DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER)));

        // {T}: Add one mana of any color.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ));
    }
}
