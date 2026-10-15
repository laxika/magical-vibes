package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "205")
@CardRegistration(set = "VOC", collectorNumber = "20")
@CardRegistration(set = "VOC", collectorNumber = "58")
public class OliviasWrath extends Card {

    public OliviasWrath() {
        PermanentCount vampiresYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE), CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(
                new Scaled(vampiresYouControl, -1),
                new Scaled(vampiresYouControl, -1),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE))))));
    }
}
