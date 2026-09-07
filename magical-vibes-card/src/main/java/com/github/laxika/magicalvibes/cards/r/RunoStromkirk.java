package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.k.KrothussLordOfTheDeep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingTransformEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "246")
public class RunoStromkirk extends Card {

    public RunoStromkirk() {
        setBackFaceCard(new KrothussLordOfTheDeep());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(new CardTypePredicate(CardType.CREATURE), 1));
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new LookAtTopCardMayRevealMatchingTransformEffect(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardMinManaValuePredicate(6)
                ))));
    }

    @Override
    public String getBackFaceClassName() {
        return "KrothussLordOfTheDeep";
    }
}
