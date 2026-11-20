package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetNameEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLA", collectorNumber = "55")
public class HonestWork extends Card {

    public HonestWork() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new TapPermanentsEffect(Subjects.attached()),
                        new RemoveAllCountersFromTargetPermanentEffect(Subjects.attached()))
                .addStaticEffect(new SetNameEffect("Humble Merchant", Subjects.attachedCreature()))
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.CITIZEN, Subjects.attachedCreature(), true))
                .addStaticEffect(new LosesAllAbilitiesEffect(Subjects.attachedCreature()))
                .addStaticEffect(new GrantActivatedAbilityEffect(
                                ManaAbilities.tapFor(ManaColor.COLORLESS),
                                Subjects.attachedCreature()))
                .addStaticEffect(new SetBasePowerToughnessEffect(1, 1, Subjects.attachedCreature()));
    }
}
