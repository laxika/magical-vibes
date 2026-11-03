package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.amount.TriggeringSpellColorCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BIG", collectorNumber = "16")
public class AncientCornucopia extends Card {

    public AncientCornucopia() {
        addTrigger(Triggers.youCastSpell(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN),
                new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardIsColorlessPredicate()),
                        List.of(new MayEffect(
                                new GainLifeEffect(new TriggeringSpellColorCount()),
                                "Gain 1 life for each of that spell's colors?"))));

        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
