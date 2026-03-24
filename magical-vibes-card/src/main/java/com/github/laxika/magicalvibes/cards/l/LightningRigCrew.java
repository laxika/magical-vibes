package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.UntapSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "150")
public class LightningRigCrew extends Card {

    public LightningRigCrew() {
        // {T}: Lightning-Rig Crew deals 1 damage to each opponent.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DealDamageToEachOpponentEffect(1)),
                "{T}: Lightning-Rig Crew deals 1 damage to each opponent."
        ));

        // Whenever you cast a Pirate spell, untap Lightning-Rig Crew.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardSubtypePredicate(CardSubtype.PIRATE),
                List.of(new UntapSelfEffect())
        ));
    }
}
