package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "EXP", collectorNumber = "3")
@CardRegistration(set = "EA3", collectorNumber = "22")
@CardRegistration(set = "MSC", collectorNumber = "266")
@CardRegistration(set = "MSC", collectorNumber = "493")
@CardRegistration(set = "ECC", collectorNumber = "168")
@CardRegistration(set = "TMC", collectorNumber = "73")
@CardRegistration(set = "WHO", collectorNumber = "307")
@CardRegistration(set = "WHO", collectorNumber = "517")
@CardRegistration(set = "WHO", collectorNumber = "898")
@CardRegistration(set = "WHO", collectorNumber = "1108")
@CardRegistration(set = "PIP", collectorNumber = "292")
@CardRegistration(set = "PIP", collectorNumber = "510")
@CardRegistration(set = "PIP", collectorNumber = "820")
@CardRegistration(set = "PIP", collectorNumber = "1038")
@CardRegistration(set = "DSC", collectorNumber = "299")
@CardRegistration(set = "LTC", collectorNumber = "332")
@CardRegistration(set = "AFC", collectorNumber = "262")
@CardRegistration(set = "C20", collectorNumber = "314")
@CardRegistration(set = "FIC", collectorNumber = "425")
@CardRegistration(set = "EOC", collectorNumber = "182")
@CardRegistration(set = "VOC", collectorNumber = "183")
@CardRegistration(set = "BRC", collectorNumber = "202")
@CardRegistration(set = "SCD", collectorNumber = "319")
public class SmolderingMarsh extends Card {

    public SmolderingMarsh() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.BASIC)
                ))),
                new EntersTappedEffect()));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
    }
}
