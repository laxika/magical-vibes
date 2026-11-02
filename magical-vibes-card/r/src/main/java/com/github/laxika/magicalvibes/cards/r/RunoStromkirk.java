package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.k.KrothussLordOfTheDeep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayRevealMatchingTransformEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "246")
@CardRegistration(set = "DBL", collectorNumber = "513")
public class RunoStromkirk extends Card {

    public RunoStromkirk() {
        setBackFaceCard(new KrothussLordOfTheDeep());

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(new CardTypePredicate(CardType.CREATURE), 1));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
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
