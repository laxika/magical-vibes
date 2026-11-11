package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "173")
@CardRegistration(set = "AKR", collectorNumber = "194")
public class HoodedBrawler extends Card {

    public HoodedBrawler() {
        // Exert: "You may exert this creature as it attacks. When you do, it gets +2/+2 until end of turn."
        // Modeled as an optional attack trigger (matching Nef-Crop Entangler / Gust Walker). Choosing to
        // exert also keeps the creature tapped through its next untap step.
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        new SkipNextUntapEffect(TapUntapScope.SELF, null, 1, false, false, true),
                        new QueueReflexiveAbilityEffect(new BoostSelfEffect(2, 2))
                ),
                "Exert Hooded Brawler as it attacks? (It gets +2/+2 until end of turn.)"
        ));
    }
}
