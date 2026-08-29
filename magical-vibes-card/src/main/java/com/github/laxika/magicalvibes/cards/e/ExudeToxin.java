package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

public class ExudeToxin extends Card {

    public ExudeToxin() {
        var minusX = new Scaled(new XValue(), -1);
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(
                minusX,
                minusX,
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DRAGON))));
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }
}
