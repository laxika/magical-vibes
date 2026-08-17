package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "325")
@CardRegistration(set = "5ED", collectorNumber = "417")
@CardRegistration(set = "FEM", collectorNumber = "96")
@CardRegistration(set = "FEM", collectorNumber = "181")
public class HavenwoodBattleground extends Card {

    public HavenwoodBattleground() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {T}: Add {G}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));

        // {T}, Sacrifice Havenwood Battleground: Add {G}{G}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new AwardManaEffect(ManaColor.GREEN, 2)
                ),
                "{T}, Sacrifice Havenwood Battleground: Add {G}{G}."
        ));
    }
}
