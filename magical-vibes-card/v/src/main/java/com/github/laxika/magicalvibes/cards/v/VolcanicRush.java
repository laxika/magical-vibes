package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "DTK", collectorNumber = "166")
public class VolcanicRush extends Card {

    public VolcanicRush() {
        PermanentIsAttackingPredicate attacking = new PermanentIsAttackingPredicate();
        addSpellEffect(BoostEffect.allCreatures(2, 0, attacking));
        addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.allCreatures(), attacking));
    }
}
