package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CreaturesDevoured;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;

@CardRegistration(set = "PC2", collectorNumber = "106")
@CardRegistration(set = "PCA", collectorNumber = "106")
public class ThromokTheInsatiable extends Card {

    public ThromokTheInsatiable() {
        // Devour X, where X is the number of creatures devoured this way.
        addStaticEffect(new DevourEffect(new CreaturesDevoured()));
    }
}
