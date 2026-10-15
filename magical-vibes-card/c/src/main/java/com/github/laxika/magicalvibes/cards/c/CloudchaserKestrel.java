package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "10")
public class CloudchaserKestrel extends Card {

    public CloudchaserKestrel() {
        target(TargetFilters.enchantment())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new GrantColorUntilEndOfTurnEffect(CardColor.WHITE)),
                "{W}: Target permanent becomes white until end of turn.",
                TargetFilters.permanent()
        ));
    }
}
