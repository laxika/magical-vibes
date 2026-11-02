package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachSourceFortificationToTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "167")
public class DarksteelGarrison extends Card {

    public DarksteelGarrison() {
        addStaticEffect(new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ENCHANTED_PERMANENT));
        addTrigger(Triggers.enchantedPermanentBecomesTapped(), new BoostTargetCreatureEffect(1, 1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new AttachSourceFortificationToTargetLandEffect()),
                "Fortify {3}: Attach this Fortification to target land you control. Activate only as a sorcery.",
                TargetFilters.landYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
