package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class LendAHam extends Card {

    public LendAHam() {
        target(TargetFilters.creature())
                .addSpellEffect(new ExileTargetPermanentEffect())
                .addSpellEffect(new GainLifeEffect(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.BOAR), CountScope.CONTROLLER)));
    }
}
