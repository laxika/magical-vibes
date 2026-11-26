package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

public class ExudeToxin extends Card {

    public ExudeToxin() {
        var minusX = new Scaled(new XValue(), -1);
        addSpellEffect(BoostEffect.allCreatures(
                minusX,
                minusX,
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DRAGON))));
        addSpellEffect(new ShuffleIntoLibraryEffect());
    }
}
