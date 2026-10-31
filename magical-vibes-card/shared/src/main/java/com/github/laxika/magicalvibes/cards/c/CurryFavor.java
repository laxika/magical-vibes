package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

public class CurryFavor extends Card {

    public CurryFavor() {
        PermanentCount knights = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.KNIGHT), CountScope.CONTROLLER);
        addSpellEffect(new GainLifeEffect(knights));
        addSpellEffect(new LoseLifeEffect(knights, LoseLifeRecipient.EACH_OPPONENT));
    }
}
