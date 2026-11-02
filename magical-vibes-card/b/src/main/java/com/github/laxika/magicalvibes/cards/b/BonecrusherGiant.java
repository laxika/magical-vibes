package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.Stomp;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringSpellControllerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ELD", collectorNumber = "115")
@CardRegistration(set = "ELD", collectorNumber = "291")
public class BonecrusherGiant extends Card {

    public BonecrusherGiant() {
        setBackFaceCard(new Stomp());
        addCastingOption(new AdventureCast("{1}{R}"));
        addTrigger(Triggers.becomesTargetOfSpell(),
                new DealDamageToTriggeringSpellControllerEffect(2));
    }

    @Override
    public String getBackFaceClassName() {
        return "Stomp";
    }
}
