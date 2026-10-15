package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "140")
public class MishraExcavationProdigy extends Card {

    public MishraExcavationProdigy() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DiscardCardTypeCost(null, null), new DrawCardEffect(1)),
                "{1}, {T}, Discard a card: Draw a card."
        ));

        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS,
                new TriggeringCardConditionalEffect(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new OncePerTurnTriggerEffect(new AwardManaEffect(ManaColor.RED, 2))));
    }
}
