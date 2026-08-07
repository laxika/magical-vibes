package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardUncounterableGrantingManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "273")
public class BoseijuWhoSheltersAll extends Card {

    public BoseijuWhoSheltersAll() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayLifeCost(2),
                        new AwardUncounterableGrantingManaEffect(ManaColor.COLORLESS, 1)
                ),
                "{T}, Pay 2 life: Add {C}. If that mana is spent on an instant or sorcery spell, that spell can't be countered."
        ));
    }
}
