package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayRevealSubtypeFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "215")
public class InvasionOfTheGiants extends Card {

    public InvasionOfTheGiants() {
        addTrigger(Triggers.chapter(1), new ScryEffect(2));
        addTrigger(Triggers.chapter(2), new DrawCardEffect(1));
        addTrigger(Triggers.chapter(2), new MayRevealSubtypeFromHandEffect(
                CardSubtype.GIANT,
                new QueueReflexiveAbilityEffect(
                        new DealDamageToTargetPlayerOrPlaneswalkerEffect(2, PlayerRelation.OPPONENT)),
                "Reveal a Giant card from your hand to deal 2 damage?"));
        addTrigger(Triggers.chapter(3),
                new ReduceCastCostForNextMatchingSpellEffect(new CardSubtypePredicate(CardSubtype.GIANT), 2));
    }
}
