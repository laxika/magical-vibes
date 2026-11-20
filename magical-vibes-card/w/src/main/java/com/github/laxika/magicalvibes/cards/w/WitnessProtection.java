package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.SetNameEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "168")
@CardRegistration(set = "SNC", collectorNumber = "66")
public class WitnessProtection extends Card {

    public WitnessProtection() {
        target(TargetFilters.creature())
                .addStaticEffect(new SetNameEffect("Legitimate Businessperson", Subjects.attachedCreature()))
                .addStaticEffect(new SetCardTypesEffect(Set.of(CardType.CREATURE), Subjects.attachedCreature()))
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.CITIZEN, Subjects.attachedCreature(), true))
                .addStaticEffect(new BecomeColorlessEffect(Subjects.attachedCreature()))
                .addStaticEffect(new GrantColorEffect(CardColor.GREEN, Subjects.attachedCreature()))
                .addStaticEffect(new GrantColorEffect(CardColor.WHITE, Subjects.attachedCreature()))
                .addStaticEffect(new LosesAllAbilitiesEffect(Subjects.attachedCreature()))
                .addStaticEffect(new SetBasePowerToughnessEffect(1, 1, Subjects.attachedCreature()));
    }
}
