package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutTriggeringSpellOnBottomThenRevealEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "245")
@CardRegistration(set = "HBG", collectorNumber = "288")
public class NeeraWildMage extends Card {

    public NeeraWildMage() {
        addTrigger(Triggers.youCastSpell().anySpell(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new MayEffect(new PutTriggeringSpellOnBottomThenRevealEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND))), "Put it on the bottom of its owner's library?"));
    }
}
