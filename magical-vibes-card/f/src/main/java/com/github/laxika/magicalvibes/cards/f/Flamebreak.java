package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "DST", collectorNumber = "61")
public class Flamebreak extends Card {

    public Flamebreak() {
        addSpellEffect(DealDamageEffect.toEachCreature(3,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))
                .andTo(Subjects.eachPlayer()).cantBeRegenerated());
    }
}
