package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ECC", collectorNumber = "135")
public class VernalSovereign extends Card {

    public VernalSovereign() {
        PermanentCount creaturesYouControl =
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        CreateTokenEffect tokenEffect = new CreateTokenEffect(
                CardType.CREATURE, 1, "Elemental", 0, 0,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.WHITE),
                List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of(), false, false,
                Map.of(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(creaturesYouControl, creaturesYouControl)),
                List.of(), false, false, false, 0, Set.of()
        );
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, tokenEffect);
        addEffect(EffectSlot.ON_ATTACK, tokenEffect);
    }
}
