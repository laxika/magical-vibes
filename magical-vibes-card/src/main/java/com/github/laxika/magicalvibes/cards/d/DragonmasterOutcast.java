package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WWK", collectorNumber = "81")
public class DragonmasterOutcast extends Card {

    public DragonmasterOutcast() {
        // At the beginning of your upkeep, if you control six or more lands, create a 5/5 red Dragon creature token with flying.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(6, new PermanentIsLandPredicate()),
                new CreateTokenEffect("Dragon", 5, 5, CardColor.RED, List.of(CardSubtype.DRAGON),
                        Set.of(Keyword.FLYING), Set.of())));
    }
}
