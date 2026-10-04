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

@CardRegistration(set = "EXP", collectorNumber = "2")
@CardRegistration(set = "EA3", collectorNumber = "23")
@CardRegistration(set = "MSC", collectorNumber = "271")
@CardRegistration(set = "MSC", collectorNumber = "498")
@CardRegistration(set = "TMC", collectorNumber = "76")
@CardRegistration(set = "WHO", collectorNumber = "312")
@CardRegistration(set = "WHO", collectorNumber = "522")
@CardRegistration(set = "WHO", collectorNumber = "903")
@CardRegistration(set = "WHO", collectorNumber = "1113")
@CardRegistration(set = "PIP", collectorNumber = "296")
@CardRegistration(set = "PIP", collectorNumber = "514")
@CardRegistration(set = "PIP", collectorNumber = "824")
@CardRegistration(set = "PIP", collectorNumber = "1042")
@CardRegistration(set = "40K", collectorNumber = "295")
@CardRegistration(set = "LTC", collectorNumber = "335")
@CardRegistration(set = "MKC", collectorNumber = "299")
@CardRegistration(set = "AFC", collectorNumber = "265")
@CardRegistration(set = "C20", collectorNumber = "318")
@CardRegistration(set = "MIC", collectorNumber = "182")
@CardRegistration(set = "C19", collectorNumber = "278")
@CardRegistration(set = "BRC", collectorNumber = "204")
@CardRegistration(set = "WOC", collectorNumber = "168")
@CardRegistration(set = "FIC", collectorNumber = "429")
@CardRegistration(set = "DRC", collectorNumber = "174")
@CardRegistration(set = "SCD", collectorNumber = "322")
public class SunkenHollow extends Card {

    public SunkenHollow() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentHasSupertypePredicate(CardSupertype.BASIC)
                ))),
                new EntersTappedEffect()));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
