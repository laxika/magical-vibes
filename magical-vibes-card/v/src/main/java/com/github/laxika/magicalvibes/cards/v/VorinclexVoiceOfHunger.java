package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "127")
@CardRegistration(set = "IMA", collectorNumber = "189")
@CardRegistration(set = "SLD", collectorNumber = "213")
@CardRegistration(set = "SLD", collectorNumber = "1623")
@CardRegistration(set = "HA5", collectorNumber = "17")
@CardRegistration(set = "MUL", collectorNumber = "29")
@CardRegistration(set = "MUL", collectorNumber = "94")
@CardRegistration(set = "MUL", collectorNumber = "159")
@CardRegistration(set = "SLD", collectorNumber = "2439")
public class VorinclexVoiceOfHunger extends Card {

    public VorinclexVoiceOfHunger() {
        // Trample is auto-loaded from Scryfall
        addTrigger(Triggers.youTapLandForMana(), new AddOneOfEachManaTypeProducedByLandEffect());
        // Whenever an opponent taps a land for mana, that land doesn't untap during its controller's next untap
        // step.
        addTrigger(Triggers.opponentTapsLandForMana(), new SkipNextUntapEffect(TapUntapScope.TRIGGERING));
    }
}
