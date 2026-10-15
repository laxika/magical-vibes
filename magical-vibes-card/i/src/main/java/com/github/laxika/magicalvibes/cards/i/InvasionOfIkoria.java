package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.z.ZilorthaApexOfIkoria;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "MOM", collectorNumber = "190")
public class InvasionOfIkoria extends Card {

    public InvasionOfIkoria() {
        setBackFaceCard(new ZilorthaApexOfIkoria());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(
                        new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardNotPredicate(new CardSubtypePredicate(CardSubtype.HUMAN)))),
                        new ManaValueBound(false, 0)));
    }

    @Override
    public String getBackFaceClassName() {
        return "ZilorthaApexOfIkoria";
    }
}
