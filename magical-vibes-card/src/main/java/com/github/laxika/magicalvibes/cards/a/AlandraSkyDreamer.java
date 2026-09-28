package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKC", collectorNumber = "94")
public class AlandraSkyDreamer extends Card {

    public AlandraSkyDreamer() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD,
                new CreateTokenEffect("Drake", 2, 2, CardColor.BLUE,
                        List.of(CardSubtype.DRAKE), Set.of(Keyword.FLYING), Set.of()));

        CardsInHand cardsInHand = new CardsInHand(CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new NthCardDrawTriggerEffect(5, SequenceEffect.of(
                        new BoostSelfEffect(cardsInHand, cardsInHand),
                        new BoostAllOwnCreaturesEffect(cardsInHand, cardsInHand,
                                new PermanentHasSubtypePredicate(CardSubtype.DRAKE)))));
    }
}
