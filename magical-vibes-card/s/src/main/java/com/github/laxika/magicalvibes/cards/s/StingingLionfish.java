package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "69")
@CardRegistration(set = "HBG", collectorNumber = "915")
public class StingingLionfish extends Card {

    public StingingLionfish() {
        // Whenever you cast your first spell during each opponent's turn, you may tap or untap
        // target nonland permanent.
        target(TargetFilters.nonlandPermanent()).addTrigger(Triggers.youCastSpell(),
                new MayEffect(
                        new SpellCastTriggerEffect(null,
                                List.of(new TapOrUntapTargetPermanentEffect()),
                                null, TargetFilters.nonlandPermanent(), null, true, false, null, 1),
                        "Tap or untap target nonland permanent?"));
    }
}
