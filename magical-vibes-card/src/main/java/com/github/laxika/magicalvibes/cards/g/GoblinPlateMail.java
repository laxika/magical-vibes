package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AmassGoblinsEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "157")
public class GoblinPlateMail extends Card {

    public GoblinPlateMail() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AmassGoblinsEffect(1));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AttachSourceEquipmentToChosenPermanentEffect());
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 0, Set.of(Keyword.MENACE), GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
