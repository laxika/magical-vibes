package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "63")
public class PollywogSymbiote extends Card {

    public PollywogSymbiote() {
        var mutateCreature = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardKeywordPredicate(Keyword.MUTATE)));

        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                mutateCreature, 1, CostModificationScope.SELF));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                mutateCreature, List.of(
                        new DrawCardEffect(1),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER))));
    }
}
