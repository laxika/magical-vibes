package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayRevealSubtypeFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

@CardRegistration(set = "KHM", collectorNumber = "215")
public class InvasionOfTheGiants extends Card {

    public InvasionOfTheGiants() {
        addEffect(EffectSlot.SAGA_CHAPTER_I, new ScryEffect(2));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new DrawCardEffect(1));
        addEffect(EffectSlot.SAGA_CHAPTER_II, new MayRevealSubtypeFromHandEffect(
                CardSubtype.GIANT,
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(2, PlayerRelation.OPPONENT),
                "Reveal a Giant card from your hand to deal 2 damage?"));
        addEffect(EffectSlot.SAGA_CHAPTER_III,
                new ReduceCastCostForNextMatchingSpellEffect(new CardSubtypePredicate(CardSubtype.GIANT), 2));
    }
}
