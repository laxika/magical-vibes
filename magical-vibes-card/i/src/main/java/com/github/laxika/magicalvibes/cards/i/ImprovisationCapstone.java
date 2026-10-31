package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ImprovisationCapstoneEffect;

@CardRegistration(set = "SOS", collectorNumber = "120")
@CardRegistration(set = "SOS", collectorNumber = "294")
public class ImprovisationCapstone extends Card {

    public ImprovisationCapstone() {
        addSpellEffect(new ImprovisationCapstoneEffect(4));
    }
}
