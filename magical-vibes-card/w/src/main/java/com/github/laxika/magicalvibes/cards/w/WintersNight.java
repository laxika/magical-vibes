package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALL", collectorNumber = "114")
@CardRegistration(set = "ME2", collectorNumber = "200")
public class WintersNight extends Card {

    public WintersNight() {
        // Whenever a player taps a snow land for mana, that player adds one mana of any type that land produced.
        // That land doesn't untap during its controller's next untap step.
        addTrigger(Triggers.playerTapsLandForMana().withFilter(new PermanentHasSupertypePredicate(CardSupertype.SNOW)),
                SequenceEffect.of(new AddOneOfEachManaTypeProducedByLandEffect(),
                        new SkipNextUntapEffect(TapUntapScope.TRIGGERING)));
    }
}
