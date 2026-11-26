package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "150")
public class LightningRigCrew extends Card {

    public LightningRigCrew() {
        // {T}: Lightning-Rig Crew deals 1 damage to each opponent.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(DealDamageEffect.to(Subjects.eachOpponent(), 1)),
                "{T}: Lightning-Rig Crew deals 1 damage to each opponent."
        ));

        // Whenever you cast a Pirate spell, untap Lightning-Rig Crew.
        addTrigger(Triggers.youCastSpell().withFilter(new CardSubtypePredicate(CardSubtype.PIRATE)),
                new UntapPermanentsEffect(Subjects.source()));
    }
}
