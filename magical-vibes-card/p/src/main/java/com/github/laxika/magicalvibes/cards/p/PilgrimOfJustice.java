package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChosenSourcePreventionScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ODY", collectorNumber = "40")
public class PilgrimOfJustice extends Card {

    public PilgrimOfJustice() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.RED)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(
                        new SacrificeSelfCost(),
                        new PreventDamageFromChosenSourceEffect(
                                ChosenSourcePreventionScope.NEXT_DAMAGE_TO_ANY_TARGET,
                                false,
                                false,
                                new PermanentColorInPredicate(Set.of(CardColor.RED)),
                                "red",
                                false,
                                false,
                                false,
                                false)
                ),
                "{W}, Sacrifice this creature: The next time a red source of your choice would deal damage this turn, prevent that damage."
        ));
    }
}
