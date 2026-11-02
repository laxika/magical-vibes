package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AER", collectorNumber = "64")
@CardRegistration(set = "KLR", collectorNumber = "94")
@CardRegistration(set = "BRC", collectorNumber = "108")
public class HeraldOfAnguish extends Card {

    public HeraldOfAnguish() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact"),
                        new BoostTargetCreatureEffect(-2, -2)
                ),
                "{1}{B}, Sacrifice an artifact: Target creature gets -2/-2 until end of turn.",
                TargetFilters.creature()
        ));
    }
}
