package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "36")
@CardRegistration(set = "FIN", collectorNumber = "359")
public class SummonKnightsOfRound extends Card {

    private static final PermanentAllOfPredicate OTHER_CREATURES = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

    private static final CreateTokenEffect KNIGHTS = new CreateTokenEffect(
            3, "Knight", 2, 2, CardColor.WHITE, List.of(CardSubtype.KNIGHT), Set.of(), Set.<CardType>of());

    public SummonKnightsOfRound() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, KNIGHTS);
        addEffect(EffectSlot.SAGA_CHAPTER_II, KNIGHTS);
        addEffect(EffectSlot.SAGA_CHAPTER_III, KNIGHTS);
        addEffect(EffectSlot.SAGA_CHAPTER_IV, KNIGHTS);
        addEffect(EffectSlot.SAGA_CHAPTER_V, new BoostAllOwnCreaturesEffect(2, 2, OTHER_CREATURES));
        addEffect(EffectSlot.SAGA_CHAPTER_V,
                new PutCounterOnEachControlledPermanentEffect(CounterType.INDESTRUCTIBLE, 1, OTHER_CREATURES));
    }
}
