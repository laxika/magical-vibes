package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M11", collectorNumber = "55")
@CardRegistration(set = "M12", collectorNumber = "55")
@CardRegistration(set = "SLD", collectorNumber = "220")
@CardRegistration(set = "HA7", collectorNumber = "3")
@CardRegistration(set = "C14", collectorNumber = "112")
public class FrostTitan extends Card {

    public FrostTitan() {
        // Whenever Frost Titan becomes the target of a spell or ability an opponent controls,
        // counter that spell or ability unless its controller pays {2}.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessPaysEffect(2));

        // Whenever Frost Titan enters the battlefield or attacks,
        // tap target permanent. It doesn't untap during its controller's next untap step.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(TapUntapScope.TARGET));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SkipNextUntapEffect(TapUntapScope.TARGET));
        addTrigger(Triggers.attacks(), new TapPermanentsEffect(TapUntapScope.TARGET));
        addTrigger(Triggers.attacks(), new SkipNextUntapEffect(TapUntapScope.TARGET));
    }
}
