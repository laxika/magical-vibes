package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BLB", collectorNumber = "120")
public class WickTheWhorledMind extends Card {

    public WickTheWhorledMind() {
        var rat = new CardSubtypePredicate(CardSubtype.RAT);
        var snail = new PermanentHasSubtypePredicate(CardSubtype.SNAIL);

        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(rat,
                        new ConditionalReplacementEffect(
                                new ControlsPermanent(snail),
                                new CreateTokenEffect("Snail", 1, 1, CardColor.BLACK,
                                        List.of(CardSubtype.SNAIL), Set.of(), Set.of()),
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1, snail))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}{B}{R}",
                List.of(
                        new SacrificePermanentCost(snail, "Sacrifice a Snail", false, true),
                        new DealDamageToPlayersEffect(new XValue(), DamageRecipient.EACH_OPPONENT),
                        new DrawCardEffect(new XValue())
                ),
                "{U}{B}{R}, Sacrifice a Snail: Wick deals damage equal to the sacrificed creature's power to each opponent. Then draw cards equal to the sacrificed creature's power."
        ));
    }
}
