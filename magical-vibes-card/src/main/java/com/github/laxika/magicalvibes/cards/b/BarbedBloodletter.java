package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "86")
public class BarbedBloodletter extends Card {

    public BarbedBloodletter() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AttachSourceEquipmentToTargetCreatureEffect())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GrantKeywordEffect(Keyword.WITHER, GrantScope.TARGET));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 2, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
