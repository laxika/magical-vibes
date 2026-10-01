package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "EXP", collectorNumber = "1")
@CardRegistration(set = "EA3", collectorNumber = "24")
@CardRegistration(set = "WHO", collectorNumber = "295")
@CardRegistration(set = "PIP", collectorNumber = "280")
@CardRegistration(set = "PIP", collectorNumber = "503")
@CardRegistration(set = "PIP", collectorNumber = "808")
@CardRegistration(set = "PIP", collectorNumber = "1031")
@CardRegistration(set = "40K", collectorNumber = "290")
@CardRegistration(set = "LTC", collectorNumber = "324")
@CardRegistration(set = "MSC", collectorNumber = "257")
@CardRegistration(set = "MSC", collectorNumber = "485")
@CardRegistration(set = "MKC", collectorNumber = "281")
@CardRegistration(set = "AFC", collectorNumber = "256")
@CardRegistration(set = "C20", collectorNumber = "299")
@CardRegistration(set = "BLC", collectorNumber = "323")
public class PrairieStream extends Card {

    public PrairieStream() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(1, new PermanentHasSupertypePredicate(CardSupertype.BASIC)),
                new EntersTappedEffect()));

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
