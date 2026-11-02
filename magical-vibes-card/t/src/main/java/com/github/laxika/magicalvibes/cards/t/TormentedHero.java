package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "108")
@CardRegistration(set = "PIO", collectorNumber = "114")
public class TormentedHero extends Card {

    public TormentedHero() {
        addStaticEffect(new EntersTappedEffect());
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                null,
                List.of(new LoseLifeEffect(1, LoseLifeRecipient.EACH_OPPONENT, true)),
                new StackEntryTargetsSourcePredicate()
        ));
    }
}
