package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "222")
public class FoggySwampSpiritKeeper extends Card {

    public FoggySwampSpiritKeeper() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD, new CreateTokenEffect(
                CardType.CREATURE, 1, "Spirit", 1, 1, null, null,
                List.of(CardSubtype.SPIRIT), Set.of(), Set.of(), false, false,
                Map.of(EffectSlot.STATIC, SequenceEffect.of(
                        new CantBlockEffect(),
                        new CantBeBlockedByCreaturesMatchingPredicateEffect(
                                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.SPIRIT))))),
                List.of(), false, false, false, 0, Set.of()));
    }
}
