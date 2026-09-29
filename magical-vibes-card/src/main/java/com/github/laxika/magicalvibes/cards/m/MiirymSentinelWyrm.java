package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "HBG", collectorNumber = "242")
@CardRegistration(set = "HBG", collectorNumber = "284")
@CardRegistration(set = "SLD", collectorNumber = "2500")
public class MiirymSentinelWyrm extends Card {

    public MiirymSentinelWyrm() {
        // Whenever another nontoken Dragon you control enters, create a nonlegendary token copy of it.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DRAGON),
                        CreateTokenCopyOfTargetPermanentEffect.nonLegendary(
                                List.of(), Set.of(), null, null, Map.of())));
    }
}
