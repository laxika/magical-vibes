package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "ARB", collectorNumber = "6")
public class FiligreeAngel extends Card {

    public FiligreeAngel() {
        // Flying is auto-loaded from Scryfall.
        // "When this creature enters, you gain 3 life for each artifact you control."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(
                new Scaled(new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER), 3)));
    }
}
