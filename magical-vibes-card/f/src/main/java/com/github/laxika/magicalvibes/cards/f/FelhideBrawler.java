package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsOtherPermanentCount;
import com.github.laxika.magicalvibes.model.effect.CantBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "BNG", collectorNumber = "70")
public class FelhideBrawler extends Card {

    public FelhideBrawler() {
        addEffect(EffectSlot.STATIC, new CantBlockUnlessEffect(
                new ControlsOtherPermanentCount(1, new PermanentHasSubtypePredicate(CardSubtype.MINOTAUR)),
                "you control another Minotaur"
        ));
    }
}
