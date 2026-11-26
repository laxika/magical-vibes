package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "7ED", collectorNumber = "228")
@CardRegistration(set = "9ED", collectorNumber = "228")
@CardRegistration(set = "MM2", collectorNumber = "134")
@CardRegistration(set = "P02", collectorNumber = "120")
@CardRegistration(set = "USG", collectorNumber = "228")
public class Wildfire extends Card {

    public Wildfire() {
        addSpellEffect(new SacrificePermanentsEffect(
                4, new PermanentIsLandPredicate(), Subjects.eachPlayer()));
        addSpellEffect(DealDamageEffect.toEachCreature(4));
    }
}
