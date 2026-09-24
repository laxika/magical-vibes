package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.InfernalOfferingEffect;

import java.util.List;

@CardRegistration(set = "C14", collectorNumber = "24")
public class InfernalOffering extends Card {

    public InfernalOffering() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Choose an opponent. You and that player each sacrifice a creature. Each player who sacrificed a creature this way draws two cards.",
                        new InfernalOfferingEffect(true)),
                new ChooseOneEffect.ChooseOneOption(
                        "Choose an opponent. Return a creature card from your graveyard to the battlefield, then that player returns a creature card from their graveyard to the battlefield.",
                        new InfernalOfferingEffect(false))
        )));
    }
}
