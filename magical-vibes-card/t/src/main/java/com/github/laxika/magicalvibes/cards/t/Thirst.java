package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;


@CardRegistration(set = "MIR", collectorNumber = "99")
public class Thirst extends Card {

    public Thirst() {
        target(TargetFilters.creature())
                // When this Aura enters, tap enchanted creature.
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(Subjects.attached()))
                // Enchanted creature doesn't untap during its controller's untap step.
                .addStaticEffect(DoesntUntapEffect.enchanted())
                // At the beginning of your upkeep, sacrifice this Aura unless you pay {U}.
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                        UnlessPaysEffect.unlessYouPay("{U}", new SacrificeSelfEffect()));
    }
}
