package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "285")
public class UntaidakeTheCloudKeeper extends Card {

    public UntaidakeTheCloudKeeper() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(2),
                        new AwardRestrictedManaEffect(ManaColor.COLORLESS, 2, new ManaRestriction.LegendarySpells())
                ),
                "{T}, Pay 2 life: Add {C}{C}. Spend this mana only to cast legendary spells."
        ));
    }
}
