package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CloakTopCardOfControllerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MKC", collectorNumber = "45")
@CardRegistration(set = "MKC", collectorNumber = "46")
@CardRegistration(set = "MKC", collectorNumber = "47")
@CardRegistration(set = "MKC", collectorNumber = "48")
@CardRegistration(set = "MKC", collectorNumber = "355")
@CardRegistration(set = "MKC", collectorNumber = "356")
@CardRegistration(set = "MKC", collectorNumber = "357")
@CardRegistration(set = "MKC", collectorNumber = "358")
public class RansomNote extends Card {

    public RansomNote() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SurveilEffect(1));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificeSelfCost(),
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Cloak the top card of your library",
                                        new CloakTopCardOfControllerLibraryEffect()),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Goad target creature",
                                        new GoadTargetCreatureUntilNextTurnEffect(),
                                        TargetFilters.creature()),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Draw a card",
                                        new DrawCardEffect())
                        ))
                ),
                "{2}, Sacrifice this artifact: Choose one — Cloak the top card of your library; goad target creature; or draw a card."
        ).withModalChoiceAtActivation());
    }
}
