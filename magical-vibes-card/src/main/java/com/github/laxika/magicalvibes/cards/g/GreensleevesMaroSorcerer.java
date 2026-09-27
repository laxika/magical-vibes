package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "961")
@CardRegistration(set = "SLD", collectorNumber = "2193")
public class GreensleevesMaroSorcerer extends Card {

    public GreensleevesMaroSorcerer() {
        PermanentCount landsYouControl = new PermanentCount(
                new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new ProtectionFromCardTypesEffect(Set.of(CardType.PLANESWALKER)));
        addEffect(EffectSlot.STATIC, new ProtectionFromSubtypesEffect(Set.of(CardSubtype.WIZARD)));
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(landsYouControl, landsYouControl));
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new CreateTokenEffect("Badger", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.BADGER), Set.of(), Set.of()));
    }
}
