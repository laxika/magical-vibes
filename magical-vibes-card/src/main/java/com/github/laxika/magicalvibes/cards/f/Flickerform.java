package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RAV", collectorNumber = "18")
public class Flickerform extends Card {

    public Flickerform() {
        target(TargetFilters.creature());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}{W}",
                List.of(FlickerEffect.exileEnchantedCreatureAndAurasReturnAtEndStep()),
                "{2}{W}{W}: Exile enchanted creature and all Auras attached to it. At the beginning of the next end step, return that card to the battlefield under its owner's control. If you do, return the other cards exiled this way to the battlefield under their owners' control attached to that creature."
        ));
    }
}
