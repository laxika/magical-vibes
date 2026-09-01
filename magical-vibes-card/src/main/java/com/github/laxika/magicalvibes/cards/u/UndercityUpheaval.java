package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MAT", collectorNumber = "25")
public class UndercityUpheaval extends Card {

    public UndercityUpheaval() {
        target(TargetFilters.creatureYouControl(), 0, 99)
                .addEffect(EffectSlot.SPELL, DistributeCountersAmongTargetsEffect.chosenAmongTargetCreatures(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER)));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.OWN_CREATURES));
    }
}
