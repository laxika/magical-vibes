package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "M20", collectorNumber = "93")
public class BonecladNecromancer extends Card {

    public BonecladNecromancer() {
        // When this creature enters, you may exile target creature card from a graveyard. If you
        // do, create a 2/2 black Zombie creature token.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(SequenceEffect.of(
                        ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(
                                1, new CardTypePredicate(CardType.CREATURE), false),
                        new ConditionalEffect(HappenedThisWay.exiled(), CreateTokenEffect.blackZombie(1))),
                        "Exile target creature card from a graveyard?"));
    }
}
