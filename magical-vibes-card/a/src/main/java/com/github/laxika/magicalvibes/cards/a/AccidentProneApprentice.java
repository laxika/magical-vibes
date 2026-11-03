package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YWOE", collectorNumber = "16")
public class AccidentProneApprentice extends Card {

    public AccidentProneApprentice() {
        setBackFaceCard(new AmphibianAccident());
        addCastingOption(new AdventureCast("{1}{U}"));

        CardEffect noncreatureSpellTrigger = new PerpetuallyBoostCardEffect(this, 1, 1);
        addTrigger(Triggers.youCastSpell().withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))),
                noncreatureSpellTrigger);
        addTrigger(new TriggerCondition(Triggers.youCastSpell()
                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))), AbilityZones.EXILE),
                TriggerOptions.DEFAULT,
                noncreatureSpellTrigger);
    }

    @Override
    public String getBackFaceClassName() {
        return "AmphibianAccident";
    }
}
