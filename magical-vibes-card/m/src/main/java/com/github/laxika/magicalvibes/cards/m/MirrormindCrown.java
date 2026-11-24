package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.FirstTokenCreationCopiesAttachedPermanentEffect;

@CardRegistration(set = "ECL", collectorNumber = "258")
@CardRegistration(set = "ECL", collectorNumber = "381")
public class MirrormindCrown extends Card {

    public MirrormindCrown() {
        addStaticEffect(new FirstTokenCreationCopiesAttachedPermanentEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
