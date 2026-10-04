package com.github.laxika.magicalvibes.cards.c;

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

@CardRegistration(set = "EXP", collectorNumber = "4")
@CardRegistration(set = "EA3", collectorNumber = "21")
@CardRegistration(set = "MSC", collectorNumber = "230")
@CardRegistration(set = "MSC", collectorNumber = "465")
@CardRegistration(set = "ECC", collectorNumber = "146")
@CardRegistration(set = "TMC", collectorNumber = "61")
@CardRegistration(set = "WHO", collectorNumber = "262")
@CardRegistration(set = "WHO", collectorNumber = "485")
@CardRegistration(set = "WHO", collectorNumber = "853")
@CardRegistration(set = "WHO", collectorNumber = "1076")
@CardRegistration(set = "PIP", collectorNumber = "257")
@CardRegistration(set = "PIP", collectorNumber = "490")
@CardRegistration(set = "PIP", collectorNumber = "785")
@CardRegistration(set = "PIP", collectorNumber = "1018")
@CardRegistration(set = "40K", collectorNumber = "269")
@CardRegistration(set = "TDC", collectorNumber = "350")
@CardRegistration(set = "M3C", collectorNumber = "329")
@CardRegistration(set = "MKC", collectorNumber = "255")
@CardRegistration(set = "AFC", collectorNumber = "229")
@CardRegistration(set = "LCC", collectorNumber = "323")
@CardRegistration(set = "C20", collectorNumber = "263")
@CardRegistration(set = "BLC", collectorNumber = "299")
@CardRegistration(set = "C19", collectorNumber = "236")
@CardRegistration(set = "NEC", collectorNumber = "166")
@CardRegistration(set = "FIC", collectorNumber = "380")
@CardRegistration(set = "EOC", collectorNumber = "154")
@CardRegistration(set = "SCD", collectorNumber = "295")
@CardRegistration(set = "BFZ", collectorNumber = "235")
public class CinderGlade extends Card {

    public CinderGlade() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.BASIC)
                ))),
                new EntersTappedEffect()));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
