package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;

@CardRegistration(set = "SPM", collectorNumber = "162")
@CardRegistration(set = "SPM", collectorNumber = "277")
public class DocOcksTentacles extends Card {

    public DocOcksTentacles() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(4, 4, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardMinManaValuePredicate(5),
                        new AttachSourceEquipmentToEnteringCreatureEffect()));
        addActivatedAbility(new EquipActivatedAbility("{5}"));
    }
}
