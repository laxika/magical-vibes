package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DKA", collectorNumber = "103")
public class ScorchTheFields extends Card {

    public ScorchTheFields() {
        // Destroy target land
        target(TargetFilters.land()).addSpellEffect(new DestroyTargetPermanentEffect());

        // Scorch the Fields deals 1 damage to each Human creature
        addSpellEffect(new MassDamageEffect(
                1, false, false, new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
    }
}
