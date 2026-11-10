package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenTappedForManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "8ED", collectorNumber = "286")
@CardRegistration(set = "7ED", collectorNumber = "281")
@CardRegistration(set = "USG", collectorNumber = "281")
public class VernalBloom extends Card {

    public VernalBloom() {
        // Whenever a Forest is tapped for mana, its controller adds an additional {G}.
        addTrigger(Triggers.playerTapsLandForMana().withFilter(new PermanentHasSubtypePredicate(CardSubtype.FOREST)),
                AddManaWhenTappedForManaEffect.ofItsController(ManaColor.GREEN));
    }
}
