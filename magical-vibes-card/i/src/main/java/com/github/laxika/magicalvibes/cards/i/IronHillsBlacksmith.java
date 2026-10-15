package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "HOB", collectorNumber = "16")
public class IronHillsBlacksmith extends Card {

    public IronHillsBlacksmith() {
        Map<EffectSlot, CardEffect> axeEffects = Map.of(
                EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));
        CreateTokenEffect axeToken = new CreateTokenEffect(
                CardType.ARTIFACT, 1, "Axe", 0, 0,
                null, null, List.of(CardSubtype.EQUIPMENT), Set.of(), Set.of(),
                false, false, axeEffects, List.of(new EquipActivatedAbility("{2}")),
                false, false, false, 0, Set.of());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, axeToken);
    }
}
