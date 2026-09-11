package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AddOneCounterToControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "164")
public class DocSamsonSuperPsychiatrist extends Card {

    public DocSamsonSuperPsychiatrist() {
        addEffect(EffectSlot.STATIC, new AddOneCounterToControlledPermanentsEffect());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect(new SourcePower())),
                "{T}: Add X mana of any one color, where X is Doc Samson's power."
        ));
    }
}
