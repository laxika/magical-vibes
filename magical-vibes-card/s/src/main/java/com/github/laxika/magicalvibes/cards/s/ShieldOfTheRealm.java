package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToRecipientsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DOM", collectorNumber = "228")
public class ShieldOfTheRealm extends Card {

    public ShieldOfTheRealm() {
        addStaticEffect(PreventDamageToRecipientsEffect.to(Subjects.attachedCreature()).preventing(2));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
