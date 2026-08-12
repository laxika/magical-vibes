package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.UnattachSourceEquipmentCost;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "147")
public class SurestrikeTrident extends Card {

    public SurestrikeTrident() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(
                                new UnattachSourceEquipmentCost(),
                                new DealDamageToTargetPlayerOrPlaneswalkerEffect(new SourcePower())
                        ),
                        "{T}, Unattach Surestrike Trident: This creature deals damage equal to its power to target player or planeswalker."
                ),
                GrantScope.EQUIPPED_CREATURE
        ));
        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
