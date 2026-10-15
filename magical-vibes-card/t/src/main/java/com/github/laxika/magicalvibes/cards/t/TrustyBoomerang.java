package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "260")
public class TrustyBoomerang extends Card {

    public TrustyBoomerang() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        "{1}",
                        List.of(
                                new TapPermanentsEffect(TapUntapScope.TARGET),
                                ReturnToHandEffect.grantingEquipment()
                        ),
                        "{1}, {T}: Tap target creature. Return Trusty Boomerang to its owner's hand.",
                        TargetFilters.creature()
                ),
                GrantScope.EQUIPPED_CREATURE
        ));

        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
