package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "MRD", collectorNumber = "181")
@CardRegistration(set = "2XM", collectorNumber = "259")
public class GolemSkinGauntlets extends Card {

    public GolemSkinGauntlets() {
        addStaticEffect(new AttachedBoostEffect(
                new AttachmentsOnSource(false, true), new Fixed(0), Subjects.attachedCreature(), true));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
