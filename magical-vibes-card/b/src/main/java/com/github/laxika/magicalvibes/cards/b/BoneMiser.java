package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SPG", collectorNumber = "87")
@CardRegistration(set = "SPG", collectorNumber = "97")
@CardRegistration(set = "C19", collectorNumber = "15")
public class BoneMiser extends Card {

    public BoneMiser() {
        addTrigger(Triggers.youDiscardCard(),
                new TriggeringCardConditionalEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        CreateTokenEffect.blackZombie(1)));
        addTrigger(Triggers.youDiscardCard(),
                new TriggeringCardConditionalEffect(
                        new CardTypePredicate(CardType.LAND),
                        new AwardManaEffect(ManaColor.BLACK, 2)));
        addTrigger(Triggers.youDiscardCard(),
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)))),
                        new DrawCardEffect(1)));
    }
}
