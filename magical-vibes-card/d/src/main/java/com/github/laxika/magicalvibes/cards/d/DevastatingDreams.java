package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardXCardsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "TOR", collectorNumber = "95")
public class DevastatingDreams extends Card {

    public DevastatingDreams() {
        addCastingCost(DiscardXCardsCost.random());
        addSpellEffect(new SacrificePermanentsEffect(
                new XValue(), new PermanentIsLandPredicate(), Subjects.eachPlayer()));
        addSpellEffect(DealDamageEffect.toEachCreature(new XValue()));
    }
}
