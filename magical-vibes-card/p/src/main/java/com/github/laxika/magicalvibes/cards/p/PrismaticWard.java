package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseColorOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5ED", collectorNumber = "53")
@CardRegistration(set = "ICE", collectorNumber = "47")
public class PrismaticWard extends Card {

    public PrismaticWard() {
        target(TargetFilters.creature()).addStaticEffect(
                PreventDamageToRecipientsEffect.to(Subjects.attachedCreature()).fromSourcesOfChosenColor());
        addStaticEffect(new ChooseColorOnEnterEffect());
    }
}
