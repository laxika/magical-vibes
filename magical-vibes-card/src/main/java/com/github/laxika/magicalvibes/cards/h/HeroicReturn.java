package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CreatureAttackingController;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MSC", collectorNumber = "17")
@CardRegistration(set = "MSC", collectorNumber = "309")
public class HeroicReturn extends Card {

    public HeroicReturn() {
        // This spell costs {2} less to cast if a creature is attacking you.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new CreatureAttackingController(),
                new ReduceOwnCastCostEffect(new Fixed(2))));

        // Return target creature card from your graveyard to the battlefield. If a Hero enters
        // this way, it enters with two additional +1/+1 counters on it.
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .plusOneCountersIfSubtype(CardSubtype.HERO)
                .plusOneCounterCount(2)
                .build());
    }
}
