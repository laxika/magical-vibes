package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SLD", collectorNumber = "1793")
public class Defile extends Card {

    public Defile() {
        PermanentCount swampsYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.SWAMP), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(
                new Scaled(swampsYouControl, -1), new Scaled(swampsYouControl, -1)));
    }
}
