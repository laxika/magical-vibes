package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellXValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;

import java.util.List;

@CardRegistration(set = "SOC", collectorNumber = "51")
@CardRegistration(set = "SOC", collectorNumber = "99")
public class BrassInfiniscope extends Card {

    public BrassInfiniscope() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardManaEffect(ManaColor.COLORLESS, 2),
                        new RegisterDelayedControllerSpellCastTriggerEffect(
                                new CardHasXInManaCostPredicate(),
                                List.of(
                                        new DrawCardEffect(),
                                        new GainLifeEffect(new Divided(new TriggeringSpellXValue(), 2))),
                                true,
                                false)),
                "{T}: Add {C}{C}. When you next cast a spell with {X} in its mana cost this turn, you draw a card and gain half X life, rounded down."));
    }
}
