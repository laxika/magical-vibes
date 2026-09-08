package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "FEM", collectorNumber = "82")
@CardRegistration(set = "FEM", collectorNumber = "167")
public class BalmOfRestoration extends Card {

    public BalmOfRestoration() {
        ActivatedAbility ability = new ActivatedAbility(
                true,
                "{1}",
                List.of(new SacrificeSelfCost(), new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption("You gain 2 life.", new GainLifeEffect(2)),
                        new ChooseOneEffect.ChooseOneOption(
                                "Prevent the next 2 damage that would be dealt to any target this turn.",
                                PreventDamageEffect.nextToTarget(2))
                ))),
                "{1}, {T}, Sacrifice this artifact: Choose one — You gain 2 life or prevent the next 2 damage that would be dealt to any target this turn."
        ).withModalChoiceAtActivation();
        addActivatedAbility(ability);
    }
}
