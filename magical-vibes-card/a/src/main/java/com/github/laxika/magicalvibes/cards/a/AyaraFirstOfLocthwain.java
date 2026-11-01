package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "75")
@CardRegistration(set = "ELD", collectorNumber = "350")
@CardRegistration(set = "MUL", collectorNumber = "13")
@CardRegistration(set = "MUL", collectorNumber = "78")
@CardRegistration(set = "MUL", collectorNumber = "143")
@CardRegistration(set = "FDC", collectorNumber = "86")
public class AyaraFirstOfLocthwain extends Card {

    public AyaraFirstOfLocthwain() {
        // Whenever Ayara or another black creature you control enters, each opponent loses 1 life
        // and you gain 1 life.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                        new CardColorPredicate(CardColor.BLACK),
                        SequenceEffect.of(
                                new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT),
                                new GainLifeEffect(1))));

        // {T}, Sacrifice another black creature: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentColorInPredicate(Set.of(CardColor.BLACK)))),
                                "another black creature"),
                        new DrawCardEffect(1)),
                "{T}, Sacrifice another black creature: Draw a card."));
    }
}
