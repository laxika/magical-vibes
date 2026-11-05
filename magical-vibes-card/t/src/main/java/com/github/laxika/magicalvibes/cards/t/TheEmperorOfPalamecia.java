package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "219")
@CardRegistration(set = "FIN", collectorNumber = "484")
public class TheEmperorOfPalamecia extends Card {

    public TheEmperorOfPalamecia() {
        setBackFaceCard(new TheLordMasterOfHell());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaOfColorsEffect(
                        List.of(ManaColor.BLUE, ManaColor.RED), new ManaRestriction.NoncreatureSpells())),
                "{T}: Add {U} or {R}. Spend this mana only to cast a noncreature spell."
        ));

        CardNotPredicate noncreatureSpell = new CardNotPredicate(new CardTypePredicate(CardType.CREATURE));
        addTrigger(Triggers.youCastSpell().withFilter(noncreatureSpell),
                TriggerOptions.DEFAULT.withInterveningIf(new SpellManaSpentAtLeast(4)),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new ConditionalEffect(
                        new SourceCounterThreshold(3, CounterType.PLUS_ONE_PLUS_ONE),
                        new TransformSelfEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "TheLordMasterOfHell";
    }
}
