package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentAnyColorManaEffect;

@CardRegistration(set = "MKM", collectorNumber = "258")
public class BranchOfVituGhazi extends Card {

    public BranchOfVituGhazi() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addMorph("{3}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP, new AwardPersistentAnyColorManaEffect(2));
    }
}
