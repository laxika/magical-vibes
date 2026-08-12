package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "149")
public class SwordOfLightAndShadow extends Card {

    public SwordOfLightAndShadow() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(
                Set.of(CardColor.WHITE, CardColor.BLACK), GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE,
                new GainLifeEffect(3));
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .build());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
