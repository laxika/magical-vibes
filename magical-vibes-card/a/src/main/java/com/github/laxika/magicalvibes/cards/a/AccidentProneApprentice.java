package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YWOE", collectorNumber = "16")
public class AccidentProneApprentice extends Card {

    public AccidentProneApprentice() {
        setBackFaceCard(new AmphibianAccident());
        addCastingOption(new AdventureCast("{1}{U}"));

        SpellCastTriggerEffect noncreatureSpellTrigger = new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new PerpetuallyBoostCardEffect(this, 1, 1)));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, noncreatureSpellTrigger);
        addEffect(EffectSlot.EXILE_ON_CONTROLLER_CASTS_SPELL, noncreatureSpellTrigger);
    }

    @Override
    public String getBackFaceClassName() {
        return "AmphibianAccident";
    }
}
