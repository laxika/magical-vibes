package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "131")
public class NettleDrone extends Card {

    public NettleDrone() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)),
                "{T}: Nettle Drone deals 1 damage to each opponent."
        ));

        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardIsColorlessPredicate(),
                List.of(new UntapPermanentsEffect(TapUntapScope.SELF))
        ));
    }
}
