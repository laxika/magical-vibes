package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllRadCountersFromTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "144")
@CardRegistration(set = "PIP", collectorNumber = "672")
public class SurvivorsMedKit extends Card {

    public SurvivorsMedKit() {
        ChooseOneEffect modes = new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Stimpak — Draw a card.", new DrawCardEffect()),
                new ChooseOneEffect.ChooseOneOption(
                        "Fancy Lads Snack Cakes — Create a Food token.", CreateTokenEffect.ofFoodToken(1)),
                new ChooseOneEffect.ChooseOneOption(
                        "RadAway — Target player loses all rad counters. Sacrifice this artifact.",
                        List.of(new RemoveAllRadCountersFromTargetPlayerEffect(), new SacrificeSelfEffect()))
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(modes),
                "{1}, {T}: Choose one that hasn't been chosen — Stimpak — Draw a card; Fancy Lads Snack Cakes — Create a Food token; or RadAway — Target player loses all rad counters. Sacrifice this artifact."
        ).withModalChoiceAtActivation().withModalModesMustBeUnused());
    }
}
