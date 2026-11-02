package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "38")
@CardRegistration(set = "RVR", collectorNumber = "43")
public class EyesEverywhere extends Card {

    public EyesEverywhere() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ScryEffect(1));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{U}",
                List.of(new ExchangeControlOfTargetPermanentsEffect(
                        TargetFilters.nonlandPermanent().predicate(), false, false, true)),
                "Exchange control of this enchantment and target nonland permanent. Activate only as a sorcery.",
                TargetFilters.nonlandPermanent(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
