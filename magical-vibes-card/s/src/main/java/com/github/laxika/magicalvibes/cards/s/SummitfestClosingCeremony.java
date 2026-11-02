package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifySourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YSOS", collectorNumber = "28")
public class SummitfestClosingCeremony extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));

    public SummitfestClosingCeremony() {
        setStartingIntensity(3);

        SpellCastTriggerEffect covercast = new SpellCastTriggerEffect(
                INSTANT_OR_SORCERY,
                List.of(new ConditionalEffect(
                        new SpellManaSpentAtLeast(5),
                        new IntensifySourceCardEffect(1))
                )
        );
        addTrigger(new TriggerCondition(Triggers.youCastSpell(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, covercast);
        addTrigger(new TriggerCondition(Triggers.youCastSpell(), AbilityZones.HAND), TriggerOptions.DEFAULT, covercast);
        addSpellEffect(new AwardManaEffect(ManaColor.BLUE, new SourceIntensity()));
        addSpellEffect(new AwardManaEffect(ManaColor.RED, new SourceIntensity()));
        addSpellEffect(new DrawCardEffect());
    }
}
