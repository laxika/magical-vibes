package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToOwnerBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "27")
public class ValkyriesCall extends Card {

    public ValkyriesCall() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardAllOfPredicate(List.of(
                        new CardNotPredicate(new CardIsTokenPredicate()),
                        new CardNotPredicate(new CardSubtypePredicate(CardSubtype.ANGEL)))),
                new ReturnDyingCreatureToOwnerBattlefieldEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, CardSubtype.ANGEL, Set.of(Keyword.FLYING))));
    }
}
