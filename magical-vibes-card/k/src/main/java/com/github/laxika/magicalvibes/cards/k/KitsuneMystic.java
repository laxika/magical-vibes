package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AutumnTailKitsuneSage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.EnchantedByAtLeastAuras;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CHK", collectorNumber = "28")
public class KitsuneMystic extends Card {

    public KitsuneMystic() {
        setBackFaceCard(new AutumnTailKitsuneSage());

        addTrigger(Triggers.endStep(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new EnchantedByAtLeastAuras(2)),
                new TransformToBackFaceEffect(true));
    }

    @Override
    public String getBackFaceClassName() {
        return "AutumnTailKitsuneSage";
    }
}
