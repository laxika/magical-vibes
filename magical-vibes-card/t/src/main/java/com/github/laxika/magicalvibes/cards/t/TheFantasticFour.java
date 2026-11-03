package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValuePowerOrToughnessEqualsPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "741")
public class TheFantasticFour extends Card {

    public TheFantasticFour() {
        List<ChooseOneEffect.ChooseOneOption> options = List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 0/4 colorless Wall creature token with defender",
                        new CreateTokenEffect("Wall", 0, 4, null,
                                List.of(CardSubtype.WALL), Set.of(Keyword.DEFENDER), Set.of())),
                new ChooseOneEffect.ChooseOneOption(
                        "The Fantastic Four deal 3 damage to each opponent",
                        new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put two +1/+1 counters on target creature",
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        TargetFilters.creature()),
                new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect()));

        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentIsSourceCardPredicate())),
                        ObjectRelation.YOU_CONTROL),
                new ChooseModeNotYetChosenThisTurnEffect(options));
        addTrigger(Triggers.youCastSpell(),
                new ChooseModeNotYetChosenThisTurnOnSpellCastEffect(
                        null,
                        new StackEntryManaValuePowerOrToughnessEqualsPredicate(4),
                        options));
    }
}
