package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "14")
public class GoShintaiOfSharedPurpose extends Card {

    public GoShintaiOfSharedPurpose() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new MayPayManaEffect("{1}",
                        new CreateTokenEffect(
                                new PermanentCount(
                                        new PermanentHasSubtypePredicate(CardSubtype.SHRINE),
                                        CountScope.CONTROLLER),
                                "Spirit", 1, 1, null,
                                List.of(CardSubtype.SPIRIT), Set.of(), Set.of()),
                        "Pay {1} to create a Spirit token for each Shrine you control?"));
    }
}
