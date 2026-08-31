package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "20")
public class GatherTheWhiteLotus extends Card {

    public GatherTheWhiteLotus() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.PLAINS), CountScope.CONTROLLER),
                "Ally", 1, 1, CardColor.WHITE, List.of(CardSubtype.ALLY), Set.of(), Set.of()));
        addEffect(EffectSlot.SPELL, new ScryEffect(2));
    }
}
