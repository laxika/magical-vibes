package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ChosenSourcePreventionScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "41")
public class PilgrimOfVirtue extends Card {

    public PilgrimOfVirtue() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(
                        new SacrificeSelfCost(),
                        new PreventDamageFromChosenSourceEffect(
                                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_ANY_TARGET, false, false,
                                new PermanentColorInPredicate(Set.of(CardColor.BLACK)), "black",
                                false, false, false, false)),
                "{W}, Sacrifice this creature: The next time a black source of your choice would deal damage this turn, prevent that damage."
        ));
    }
}
