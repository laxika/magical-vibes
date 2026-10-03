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
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

@CardRegistration(set = "EXP", collectorNumber = "5")
@CardRegistration(set = "EA3", collectorNumber = "20")
@CardRegistration(set = "WHO", collectorNumber = "1072")
@CardRegistration(set = "WHO", collectorNumber = "258")
@CardRegistration(set = "WHO", collectorNumber = "481")
@CardRegistration(set = "WHO", collectorNumber = "849")
@CardRegistration(set = "AFC", collectorNumber = "227")
@CardRegistration(set = "C20", collectorNumber = "261")
@CardRegistration(set = "PIP", collectorNumber = "255")
@CardRegistration(set = "PIP", collectorNumber = "488")
@CardRegistration(set = "PIP", collectorNumber = "783")
@CardRegistration(set = "PIP", collectorNumber = "1016")
@CardRegistration(set = "LTC", collectorNumber = "298")
@CardRegistration(set = "MSC", collectorNumber = "227")
@CardRegistration(set = "MSC", collectorNumber = "462")
@CardRegistration(set = "TDC", collectorNumber = "343")
@CardRegistration(set = "MKC", collectorNumber = "252")
@CardRegistration(set = "LCC", collectorNumber = "321")
@CardRegistration(set = "BLC", collectorNumber = "296")
@CardRegistration(set = "MIC", collectorNumber = "168")
@CardRegistration(set = "WOC", collectorNumber = "153")
@CardRegistration(set = "FIC", collectorNumber = "378")
public class CanopyVista extends Card {

    public CanopyVista() {
        addEffect(EffectSlot.STATIC, new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(1, new PermanentHasSupertypePredicate(CardSupertype.BASIC)),
                new EntersTappedEffect()));

        // {T}: Add {G}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));

        // {T}: Add {W}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
